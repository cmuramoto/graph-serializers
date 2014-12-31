package domain.gs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Fields;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;
import com.nc.gs.meta.Shape;

@Fields(compressByDefault = true)
public class Media implements Serializable {

	public enum Player {
		JAVA, FLASH
	}

	private static final long serialVersionUID = 1L;

	public Player player;
	@NotNull
	@OnlyPayload
	public String uri;
	@OnlyPayload
	public String title;
	public int width;
	public int height;
	public String format;
	public long duration;
	public long size;
	public int bitrate;

	@NotNull
	@OnlyPayload
	@Collection(concreteImpl = ArrayList.class, optimize = true, shape = @Shape(nullable = false, onlyPayload = true))
	public List<Person> persons;
	@OnlyPayload
	String copyright;

	public Media() {
	}

	public Media(String copyright, String format, Player player, String title, String uri, long duration, long size, int height, int width, int bitrate) {
		this.copyright = copyright;
		this.duration = duration;
		this.format = format;
		this.height = height;
		this.player = player;
		this.size = size;
		this.title = title;
		this.uri = uri;
		this.width = width;
		this.bitrate = bitrate;
	}

	public void addToPerson(String person) {
		if (null == persons) {
			persons = new ArrayList<Person>();
		}
		Person p = new Person();
		p.setName(person);
		persons.add(p);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Media other = (Media) obj;
		if (bitrate != other.bitrate) {
			return false;
		}
		if (copyright == null) {
			if (other.copyright != null) {
				return false;
			}
		} else if (!copyright.equals(other.copyright)) {
			return false;
		}
		if (duration != other.duration) {
			return false;
		}
		if (format == null) {
			if (other.format != null) {
				return false;
			}
		} else if (!format.equals(other.format)) {
			return false;
		}
		if (height != other.height) {
			return false;
		}
		if (persons == null) {
			if (other.persons != null) {
				return false;
			}
		} else if (!persons.equals(other.persons)) {
			return false;
		}
		if (player == null) {
			if (other.player != null) {
				return false;
			}
		} else if (!player.equals(other.player)) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		if (width != other.width) {
			return false;
		}
		return true;
	}

	public int getBitrate() {
		return bitrate;
	}

	public String getCopyright() {
		return copyright;
	}

	public long getDuration() {
		return duration;
	}

	public String getFormat() {
		return format;
	}

	public int getHeight() {
		return height;
	}

	public List<Person> getPersons() {
		return persons;
	}

	public Player getPlayer() {
		return player;
	}

	public long getSize() {
		return size;
	}

	public String getTitle() {
		return title;
	}

	public String getUri() {
		return uri;
	}

	public int getWidth() {
		return width;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bitrate;
		result = prime * result + (copyright == null ? 0 : copyright.hashCode());
		result = prime * result + (int) (duration ^ duration >>> 32);
		result = prime * result + (format == null ? 0 : format.hashCode());
		result = prime * result + height;
		result = prime * result + (persons == null ? 0 : persons.hashCode());
		result = prime * result + (player == null ? 0 : player.hashCode());
		result = prime * result + (int) (size ^ size >>> 32);
		result = prime * result + (title == null ? 0 : title.hashCode());
		result = prime * result + (uri == null ? 0 : uri.hashCode());
		result = prime * result + width;
		return result;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setPersons(List<String> p) {
		persons = p.stream().map(n -> new Person(n)).collect(Collectors.toList());
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Media ");
		sb.append("width=").append(width);
		sb.append(", height=").append(height);
		sb.append(", duration=").append(duration);
		sb.append(", size=").append(size);
		sb.append(", bitrate=").append(bitrate);
		sb.append(", player=").append(player);
		sb.append(", uri=").append(uri);
		sb.append(", title=").append(title);
		sb.append(", format=").append(format);
		sb.append(", persons=").append(persons);
		sb.append(", copyright=").append(copyright);
		sb.append("]");
		return sb.toString();
	}
}
