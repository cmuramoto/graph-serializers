#
# Autogenerated by Thrift Compiler (0.9.0)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#

require 'thrift'

module Size
  SMALL = 1
  LARGE = 2
  VALUE_MAP = {1 => "SMALL", 2 => "LARGE"}
  VALID_VALUES = Set.new([SMALL, LARGE]).freeze
end

module Player
  JAVA = 0
  FLASH = 1
  VALUE_MAP = {0 => "JAVA", 1 => "FLASH"}
  VALID_VALUES = Set.new([JAVA, FLASH]).freeze
end

# Some comment...
class Person
  include ::Thrift::Struct, ::Thrift::Struct_Union
  NAME = 1

  FIELDS = {
    NAME => {:type => ::Thrift::Types::STRING, :name => 'name'}
  }

  def struct_fields; FIELDS; end

  def validate
  end

  ::Thrift::Struct.generate_accessors self
end

class Image
  include ::Thrift::Struct, ::Thrift::Struct_Union
  URI = 1
  TITLE = 2
  WIDTH = 3
  HEIGHT = 4
  SIZE = 5

  FIELDS = {
    URI => {:type => ::Thrift::Types::STRING, :name => 'uri'},
    TITLE => {:type => ::Thrift::Types::STRING, :name => 'title', :optional => true},
    WIDTH => {:type => ::Thrift::Types::I32, :name => 'width', :optional => true},
    HEIGHT => {:type => ::Thrift::Types::I32, :name => 'height', :optional => true},
    SIZE => {:type => ::Thrift::Types::I32, :name => 'size', :optional => true, :enum_class => ::Size}
  }

  def struct_fields; FIELDS; end

  def validate
    unless @size.nil? || ::Size::VALID_VALUES.include?(@size)
      raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field size!')
    end
  end

  ::Thrift::Struct.generate_accessors self
end

class Media
  include ::Thrift::Struct, ::Thrift::Struct_Union
  URI = 1
  TITLE = 2
  WIDTH = 3
  HEIGHT = 4
  FORMAT = 5
  DURATION = 6
  SIZE = 7
  BITRATE = 8
  PERSON = 9
  PLAYER = 10
  COPYRIGHT = 11

  FIELDS = {
    URI => {:type => ::Thrift::Types::STRING, :name => 'uri'},
    TITLE => {:type => ::Thrift::Types::STRING, :name => 'title', :optional => true},
    WIDTH => {:type => ::Thrift::Types::I32, :name => 'width', :optional => true},
    HEIGHT => {:type => ::Thrift::Types::I32, :name => 'height', :optional => true},
    FORMAT => {:type => ::Thrift::Types::STRING, :name => 'format', :optional => true},
    DURATION => {:type => ::Thrift::Types::I64, :name => 'duration', :optional => true},
    SIZE => {:type => ::Thrift::Types::I64, :name => 'size', :optional => true},
    BITRATE => {:type => ::Thrift::Types::I32, :name => 'bitrate', :optional => true},
    PERSON => {:type => ::Thrift::Types::LIST, :name => 'person', :element => {:type => ::Thrift::Types::STRUCT, :class => ::Person}, :optional => true},
    PLAYER => {:type => ::Thrift::Types::I32, :name => 'player', :optional => true, :enum_class => ::Player},
    COPYRIGHT => {:type => ::Thrift::Types::STRING, :name => 'copyright', :optional => true}
  }

  def struct_fields; FIELDS; end

  def validate
    unless @player.nil? || ::Player::VALID_VALUES.include?(@player)
      raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field player!')
    end
  end

  ::Thrift::Struct.generate_accessors self
end

class MediaContent
  include ::Thrift::Struct, ::Thrift::Struct_Union
  IMAGE = 1
  MEDIA = 2

  FIELDS = {
    IMAGE => {:type => ::Thrift::Types::LIST, :name => 'image', :element => {:type => ::Thrift::Types::STRUCT, :class => ::Image}, :optional => true},
    MEDIA => {:type => ::Thrift::Types::STRUCT, :name => 'media', :class => ::Media, :optional => true}
  }

  def struct_fields; FIELDS; end

  def validate
  end

  ::Thrift::Struct.generate_accessors self
end

