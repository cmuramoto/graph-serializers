graph-serializers
=================

#### Goals

This project aims to provide high performance java serialization by performing class analysis and creating the corresponding efficient serializers on the fly, aiming for zero-reflection whenever possible.

This framework is a non-invasive, inheritance-based serialization solution, that tries its hardest to be as efficient as frameworks that rely on static schemas such as protocol buffers and thrift.

##### Features

This library is intended to be used as a serialization library for (possibly) cyclic domain models and can be optimized to work with flat models (no cyclic detection) with a field-level glanularity. For example, supose we have the following class:

```java
public class Node {
  
  Node left;
  
  Node right;
  
  String label;
}
```

Let's say left and right might be cyclic, however we know that the *label* will never be repeated when serializing a graph of nodes. In this case, we can just forfeit reference marking of the label field in order to reduce a bit the processing overhead and as well as save a few bytes on the final binary payload of the object graph. To let go reference marking of the *label* field we just have to decorate it with some metadata.

```java
public class Node {
  
  Node left;
  
  Node right;
  
  @OnlyPayload
  String label;
}
```

#### Simple Usage

There are a few ways one can use the API to read/write objects. The simplest is probably via the **Context** class.

```java
public class Main {
  
  public static void main(String...args){
  	Node n=...
  	Sink dst=new Sink();
  	
  	try(Context c=Context.writing()){
  		c.writeRoot(dst,n);
  	}
  	
  	byte[] buff=dst.toByteArray();
  	
  	Source src=new Source();
  	
  	try(Context c=Context.reading()){
  		Node rec=c.readRoot(src.filledWith(buff),Node.class);
  		//...
  	}
  	
  	sink.flushTo(//...some output stream);
  }
}
```




#### Sinks, Sources and (no) Streaming

The Sink and Source extend (Output/Input)Stream as well as implement Data(Output/Input), however as of now they are meant to be used more like direct ByteBuffers. The reason for this is to avoid flushing and filling logic during (des-)serialization. So, during serialization everything will be done (and) held in memory until one decides to commit the data to another I/O target and during the deserialization the whole source stream should be consumed beforehand.

This approach might be troublesome for very large objects, but in the real world one should just not serialize huge objects!





#### Benchmarks



##### The Numbers

The numbers in the charts bellow correspond to a slightly modified version of the old [thrift x protobuff](http://thrift-protobuf-compare.googlecode.com/svn/trunk/) benchmark code. In practice, this benchmark is almost all about String serialization performance.

It should be noted that protobuff uses a nice internal representation for a String called ByteString, which is ultra-fast to be (de-)serialized however in practice one should take into account the *pre-processing* time required by the framework to perform the conversion *String->ByteString* during the construction of an Object (Message)  and the *post-processing* in order for one to get a java.lang.String out of a com.google.protobuf.ByteString after deserialization.

The following chart displays the overall serialization cost (Time to instantiate+Serialize)

![][ser]


Next the deserialization time with and without round-trip validation. 

![][des]
![][desall]

In the first case, protobuf has a HUGE advantage because it is only deserializing it's ByteStrings, which comes down to just copying bytes from a stream without any processing involved. However, during round-trip validation, every field has to pass a simple assertion test, which implies that some conversions *ByteString->String* end up taking a considerable amount of time.

If we give up String compression during (de-)serialization, by writing the contents of a String as a 'raw' char[], we end up with the following numbers:

![][des-stringopt]

![][desall-stringopt]

So, as we can see this benchmark is more about String serialization performance than anything, however we are able to match the messages generated by *protoc* and even surpass it, considerably, if we let go String compression.

##### The model

For the benchmarks we have used the following structure protocol buffer messages: 

```c
package domain.protobuf;

option java_package = "domain.protobuf";
option java_outer_classname = "MediaContentHolder";
option optimize_for = SPEED;

message Image {
  required string uri = 1;      //url to the thumbnail
  optional string title = 2;    //used in the html ALT
  optional int32 width = 3;     // of the image
  optional int32 height = 4;    // of the image
  enum Size {
    SMALL = 0;
    LARGE = 1;
  }
  optional Size size = 5;       // of the image (in relative terms, provided by cnbc for example)
}

message Person {
  required string name = 1;      //url to the thumbnail
}  
  

message Media {
  required string uri = 1;      //uri to the video, may not be an actual URL
  optional string title = 2;    //used in the html ALT
  optional int32 width = 3;     // of the video
  optional int32 height = 4;    // of the video
  optional string format = 5;   //avi, jpg, youtube, cnbc, audio/mpeg formats ...
  optional int64 duration = 6;  //time in miliseconds
  optional int64 size = 7;      //file size
  optional int32 bitrate = 8;   //video 
  repeated Person person = 9;   //name of a person featured in the video
  enum Player {
    JAVA = 0;
    FLASH = 1;
  }
  optional Player player = 10;   //in case of a player specific media
  optional string copyright = 11;//media copyright
}

message MediaContent {
  repeated Image image = 1;
  optional Media media = 2;
}
```

Likewise, for the Thrift code we have used the following media.thrift file:

```c
namespace java domain.thrift

typedef i32 int
typedef i64 long

enum Size {
  SMALL = 1,
  LARGE = 2,
}

enum Player {
  JAVA = 0,
  FLASH = 1,
}

/**
 * Some comment...
 */
struct Person {
  1: string name,              //name 
} 
 
struct Image {
  1: string uri,              //url to the images
  2: optional string title,  
  3: optional int width,
  4: optional int height,
  5: optional Size size,
}

struct Media {
  1: string uri,             //url to the thumbnail
  2: optional string title,
  3: optional int width,
  4: optional int height,
  5: optional string format,
  6: optional long duration,
  7: optional long size,
  8: optional int bitrate,
  9: optional list<Person> person,
  10: optional Player player,
  11: optional string copyright,
}

struct MediaContent {
  1: optional list<Image> image,
  2: optional Media media,
}

```

On the other hand, the java classes used by the framework were simple POJOs, without (almost) any metadatada in order to turn them into optimized data structures. For the **MediaContent** class the java code reads:

```java
@Fields(compressByDefault = true)
public class MediaContent implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull
	@OnlyPayload
	@Collection(concreteImpl = ArrayList.class, optimize = true, shape = @Shape(hierarchy = @Hierarchy(types = { Image.class }, complete = true), nullable = false, onlyPayload = true))
	public List<Image> images;

	@LeafNode
	@NotNull
	@OnlyPayload
	public Media media;

	//gets, sets, etc...
}


```

The annotations aren't realy necessary, however to make the benchmark more apples-to-apples we used some annotations to inform the graph-serializers framework to make certain optimizations.

### Optimizations

#### SIMD UTF-8 processing

UTF-8 to UTF-16 (Java's 'native' encoding) conversion and vice-versa can be tricky to optimize. Java algorithms usually make an 'optimistic' loop to handle ASCII characters and fall back to a more complex code once a non-Ascii char is found in the stream. The following code block is an excerpt of readUTF method from from Java's DataInputStream class:

```java

	//Ascii Loop. Once a non-Ascii char is found, fallback to 'deoptimized' code.
	while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127) break;
            count++;
            chararr[chararr_count++]=(char)c;
	}

	//More complex loop. Handle both Ascii and non-Ascii chars. 
	while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++]=(char)c;
                    break;
                case 12: case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                            "malformed input: partial character at end");
                    char2 = (int) bytearr[count-1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                            "malformed input around byte " + count);
                    chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
                                                    (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                            "malformed input: partial character at end");
                    char2 = (int) bytearr[count-2];
                    char3 = (int) bytearr[count-1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                            "malformed input around byte " + (count-1));
                    chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                                                    ((char2 & 0x3F) << 6)  |
                                                    ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException(
                        "malformed input around byte " + count);
            }
        }
```

The problem with this code is that JIT compilers aren't not yet capable of vectorizing even simple loops like the 'ideal' Ascii loop. 

When dealing with large Strings (>128 chars) vectorization can play a huge difference in serialization/deserialization performance. Even for pure-Ascii text, the speed gains can be as large as 5x! Microbenchmarks using ~5K non-pure Ascii text revealed about 9x times faster deserialization performance! 

As of now, GraphSerializers will use JNI to invoke SIMD processing during deserialization if the underlying platform is Linux (64-bit). On simple tests, SSE 4.2 and AVX2.0 implementations displayed the same performance for pure-Ascii text. The fully vectorized implementation is only available using SSE 4.2 instructions and is based on the [UTF-8 processing using SIMD (SSE4) article](http://woboq.com/blog/utf-8-processing-using-simd.html) and AVX2.0 is a work in progress. 



#### Annotations

//TODO

##### @LeafNode

##### @Hierarchy - Making inline caches

[ser]: https://cloud.githubusercontent.com/assets/7014591/5587308/06088ee6-90ce-11e4-9a3d-9c446fb0933e.png
[des]: https://cloud.githubusercontent.com/assets/7014591/5587302/05e9a9d6-90ce-11e4-8dce-69bbd5d171a6.png
[desall]: https://cloud.githubusercontent.com/assets/7014591/5587303/05eb2d6a-90ce-11e4-842f-d185b27dc0bf.png
[desall-stringopt]: https://cloud.githubusercontent.com/assets/7014591/7216779/971b6936-e5e1-11e4-86c2-44bddf8ab568.png
[des-stringopt]: https://cloud.githubusercontent.com/assets/7014591/7216780/971e31ca-e5e1-11e4-8e5a-0099cd45485d.png

[desall-stringopt-clq]: https://cloud.githubusercontent.com/assets/7014591/5587301/05e8d5f6-90ce-11e4-8642-16ffec736894.png
[des-stringopt-clq]: https://cloud.githubusercontent.com/assets/7014591/5587304/05ecb8ba-90ce-11e4-9cc0-7e74cb5aa9af.png
