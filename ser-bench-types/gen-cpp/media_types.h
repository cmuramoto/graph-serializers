/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#ifndef media_TYPES_H
#define media_TYPES_H

#include <thrift/Thrift.h>
#include <thrift/TApplicationException.h>
#include <thrift/protocol/TProtocol.h>
#include <thrift/transport/TTransport.h>





struct Size {
  enum type {
    SMALL = 1,
    LARGE = 2
  };
};

extern const std::map<int, const char*> _Size_VALUES_TO_NAMES;

struct Player {
  enum type {
    JAVA = 0,
    FLASH = 1
  };
};

extern const std::map<int, const char*> _Player_VALUES_TO_NAMES;

typedef int32_t int;

typedef int64_t long;

typedef struct _Person__isset {
  _Person__isset() : name(false) {}
  bool name;
} _Person__isset;

class Person {
 public:

  static const char* ascii_fingerprint; // = "EFB929595D312AC8F305D5A794CFEDA1";
  static const uint8_t binary_fingerprint[16]; // = {0xEF,0xB9,0x29,0x59,0x5D,0x31,0x2A,0xC8,0xF3,0x05,0xD5,0xA7,0x94,0xCF,0xED,0xA1};

  Person() : name() {
  }

  virtual ~Person() throw() {}

  std::string name;

  _Person__isset __isset;

  void __set_name(const std::string& val) {
    name = val;
  }

  bool operator == (const Person & rhs) const
  {
    if (!(name == rhs.name))
      return false;
    return true;
  }
  bool operator != (const Person &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const Person & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

void swap(Person &a, Person &b);

typedef struct _Image__isset {
  _Image__isset() : uri(false), title(false), width(false), height(false), size(false) {}
  bool uri;
  bool title;
  bool width;
  bool height;
  bool size;
} _Image__isset;

class Image {
 public:

  static const char* ascii_fingerprint; // = "EEFF150CE9C5F3131DAD35EFCC358B95";
  static const uint8_t binary_fingerprint[16]; // = {0xEE,0xFF,0x15,0x0C,0xE9,0xC5,0xF3,0x13,0x1D,0xAD,0x35,0xEF,0xCC,0x35,0x8B,0x95};

  Image() : uri(), title(), width(0), height(0), size((Size::type)0) {
  }

  virtual ~Image() throw() {}

  std::string uri;
  std::string title;
  int width;
  int height;
  Size::type size;

  _Image__isset __isset;

  void __set_uri(const std::string& val) {
    uri = val;
  }

  void __set_title(const std::string& val) {
    title = val;
    __isset.title = true;
  }

  void __set_width(const int val) {
    width = val;
    __isset.width = true;
  }

  void __set_height(const int val) {
    height = val;
    __isset.height = true;
  }

  void __set_size(const Size::type val) {
    size = val;
    __isset.size = true;
  }

  bool operator == (const Image & rhs) const
  {
    if (!(uri == rhs.uri))
      return false;
    if (__isset.title != rhs.__isset.title)
      return false;
    else if (__isset.title && !(title == rhs.title))
      return false;
    if (__isset.width != rhs.__isset.width)
      return false;
    else if (__isset.width && !(width == rhs.width))
      return false;
    if (__isset.height != rhs.__isset.height)
      return false;
    else if (__isset.height && !(height == rhs.height))
      return false;
    if (__isset.size != rhs.__isset.size)
      return false;
    else if (__isset.size && !(size == rhs.size))
      return false;
    return true;
  }
  bool operator != (const Image &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const Image & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

void swap(Image &a, Image &b);

typedef struct _Media__isset {
  _Media__isset() : uri(false), title(false), width(false), height(false), format(false), duration(false), size(false), bitrate(false), person(false), player(false), copyright(false) {}
  bool uri;
  bool title;
  bool width;
  bool height;
  bool format;
  bool duration;
  bool size;
  bool bitrate;
  bool person;
  bool player;
  bool copyright;
} _Media__isset;

class Media {
 public:

  static const char* ascii_fingerprint; // = "D50673FA5CDB5424B27DD592496DF662";
  static const uint8_t binary_fingerprint[16]; // = {0xD5,0x06,0x73,0xFA,0x5C,0xDB,0x54,0x24,0xB2,0x7D,0xD5,0x92,0x49,0x6D,0xF6,0x62};

  Media() : uri(), title(), width(0), height(0), format(), duration(0), size(0), bitrate(0), player((Player::type)0), copyright() {
  }

  virtual ~Media() throw() {}

  std::string uri;
  std::string title;
  int width;
  int height;
  std::string format;
  long duration;
  long size;
  int bitrate;
  std::vector<Person>  person;
  Player::type player;
  std::string copyright;

  _Media__isset __isset;

  void __set_uri(const std::string& val) {
    uri = val;
  }

  void __set_title(const std::string& val) {
    title = val;
    __isset.title = true;
  }

  void __set_width(const int val) {
    width = val;
    __isset.width = true;
  }

  void __set_height(const int val) {
    height = val;
    __isset.height = true;
  }

  void __set_format(const std::string& val) {
    format = val;
    __isset.format = true;
  }

  void __set_duration(const long val) {
    duration = val;
    __isset.duration = true;
  }

  void __set_size(const long val) {
    size = val;
    __isset.size = true;
  }

  void __set_bitrate(const int val) {
    bitrate = val;
    __isset.bitrate = true;
  }

  void __set_person(const std::vector<Person> & val) {
    person = val;
    __isset.person = true;
  }

  void __set_player(const Player::type val) {
    player = val;
    __isset.player = true;
  }

  void __set_copyright(const std::string& val) {
    copyright = val;
    __isset.copyright = true;
  }

  bool operator == (const Media & rhs) const
  {
    if (!(uri == rhs.uri))
      return false;
    if (__isset.title != rhs.__isset.title)
      return false;
    else if (__isset.title && !(title == rhs.title))
      return false;
    if (__isset.width != rhs.__isset.width)
      return false;
    else if (__isset.width && !(width == rhs.width))
      return false;
    if (__isset.height != rhs.__isset.height)
      return false;
    else if (__isset.height && !(height == rhs.height))
      return false;
    if (__isset.format != rhs.__isset.format)
      return false;
    else if (__isset.format && !(format == rhs.format))
      return false;
    if (__isset.duration != rhs.__isset.duration)
      return false;
    else if (__isset.duration && !(duration == rhs.duration))
      return false;
    if (__isset.size != rhs.__isset.size)
      return false;
    else if (__isset.size && !(size == rhs.size))
      return false;
    if (__isset.bitrate != rhs.__isset.bitrate)
      return false;
    else if (__isset.bitrate && !(bitrate == rhs.bitrate))
      return false;
    if (__isset.person != rhs.__isset.person)
      return false;
    else if (__isset.person && !(person == rhs.person))
      return false;
    if (__isset.player != rhs.__isset.player)
      return false;
    else if (__isset.player && !(player == rhs.player))
      return false;
    if (__isset.copyright != rhs.__isset.copyright)
      return false;
    else if (__isset.copyright && !(copyright == rhs.copyright))
      return false;
    return true;
  }
  bool operator != (const Media &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const Media & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

void swap(Media &a, Media &b);

typedef struct _MediaContent__isset {
  _MediaContent__isset() : image(false), media(false) {}
  bool image;
  bool media;
} _MediaContent__isset;

class MediaContent {
 public:

  static const char* ascii_fingerprint; // = "C827532AA0A32AFCF6D52454C8939205";
  static const uint8_t binary_fingerprint[16]; // = {0xC8,0x27,0x53,0x2A,0xA0,0xA3,0x2A,0xFC,0xF6,0xD5,0x24,0x54,0xC8,0x93,0x92,0x05};

  MediaContent() {
  }

  virtual ~MediaContent() throw() {}

  std::vector<Image>  image;
  Media media;

  _MediaContent__isset __isset;

  void __set_image(const std::vector<Image> & val) {
    image = val;
    __isset.image = true;
  }

  void __set_media(const Media& val) {
    media = val;
    __isset.media = true;
  }

  bool operator == (const MediaContent & rhs) const
  {
    if (__isset.image != rhs.__isset.image)
      return false;
    else if (__isset.image && !(image == rhs.image))
      return false;
    if (__isset.media != rhs.__isset.media)
      return false;
    else if (__isset.media && !(media == rhs.media))
      return false;
    return true;
  }
  bool operator != (const MediaContent &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const MediaContent & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

void swap(MediaContent &a, MediaContent &b);



#endif
