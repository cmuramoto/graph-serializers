package domain.gs.compressed;

import java.io.Serializable;

import com.nc.gs.meta.Fields;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;

@Fields(compressByDefault = true)
public class Image implements Serializable {
	public enum Size {
		SMALL, LARGE
	}

	private static final long serialVersionUID = 1L;

	@NotNull
	@OnlyPayload
	public String uri;
	@OnlyPayload
	public String title;
	public int width;
	public int height;
	@NotNull
	public Size size;

	public Image() {
	}

	public Image(int height, String title, String uri, int width, Size size) {
		super();
		this.height = height;
		this.title = title;
		this.uri = uri;
		this.width = width;
		this.size = size;
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
		Image other = (Image) obj;
		if (height != other.height) {
			return false;
		}
		if (size == null) {
			if (other.size != null) {
				return false;
			}
		} else if (!size.equals(other.size)) {
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

	public int getHeight() {
		return height;
	}

	public Size getSize() {
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
		result = prime * result + height;
		result = prime * result + (size == null ? 0 : size.hashCode());
		result = prime * result + (title == null ? 0 : title.hashCode());
		result = prime * result + (uri == null ? 0 : uri.hashCode());
		result = prime * result + width;
		return result;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setSize(Size size) {
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
		sb.append("[Image ");
		sb.append("width=").append(width);
		sb.append(", height=").append(height);
		sb.append(", uri=").append(uri);
		sb.append(", title=").append(title);
		sb.append(", size=").append(size);
		sb.append("]");
		return sb.toString();
	}
}
