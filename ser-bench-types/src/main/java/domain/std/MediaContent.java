package domain.std;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.LeafNode;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;
import com.nc.gs.meta.Shape;

public class MediaContent implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull
	@OnlyPayload
	@Collection(concreteImpl = ArrayList.class, optimize = true, shape = @Shape(hierarchy = @Hierarchy(types = { Image.class }, complete = true), nullable = false, onlyPayload = true))
	public List<Image> images;

	@LeafNode
	@NotNull
	public Media media;

	public MediaContent() {
	}

	public MediaContent(Media media) {
		this.media = media;
	}

	public void addImage(Image image) {
		if (images == null) {
			images = new ArrayList<Image>();
		}
		images.add(image);
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
		MediaContent other = (MediaContent) obj;
		if (images == null) {
			if (other.images != null) {
				return false;
			}
		} else if (!images.equals(other.images)) {
			return false;
		}
		if (media == null) {
			if (other.media != null) {
				return false;
			}
		} else if (!media.equals(other.media)) {
			return false;
		}
		return true;
	}

	public Image getImage(int i) {
		return images.get(i);
	}

	public List<Image> getImages() {
		return images;
	}

	public Media getMedia() {
		return media;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (images == null ? 0 : images.hashCode());
		result = prime * result + (media == null ? 0 : media.hashCode());
		return result;
	}

	public int imageCount() {
		return images.size();
	}

	public void setImages(List<Image> i) {
		images = i;
	}

	public void setMedia(Media m) {
		media = m;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[MediaContent: ");
		sb.append("media=").append(media);
		sb.append(", images=").append(images);
		sb.append("]");
		return sb.toString();
	}
}
