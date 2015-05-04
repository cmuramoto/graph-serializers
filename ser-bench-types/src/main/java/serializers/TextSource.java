package serializers;

public interface TextSource {

	static class Ascii implements TextSource {

		@Override
		public String firstPerson() {
			return "Bill Gates";
		}

		@Override
		public String largeImageUrl() {
			return "http://javaone.com/keynote_large.jpg";
		}

		@Override
		public String mediaUrl() {
			return "http://javaone.com/keynote.mpg";
		}

		@Override
		public String secondPerson() {
			return "Steve Jobs";
		}

		@Override
		public String smallImageUrl() {
			return "http://javaone.com/keynote_thumbnail.jpg";
		}

		@Override
		public String tag() {
			return "Javaone Keynote";
		}
	}

	static class LargeAscii implements TextSource {

		@Override
		public String firstPerson() {
			return "Bill Gates";
		}

		@Override
		public String largeImageUrl() {
			return "http://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpghttp://javaone.com/keynote_large.jpg";
		}

		@Override
		public String mediaUrl() {
			return "http://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpghttp://javaone.com/keynote.mpg";
		}

		@Override
		public String secondPerson() {
			return "Steve Jobs";
		}

		@Override
		public String smallImageUrl() {
			return "http://javaone.com/keynote_thumbnail.jpg";
		}

		@Override
		public String tag() {
			return "Javaone Keynote";
		}
	}

	static class Mixed implements TextSource {

		@Override
		public String firstPerson() {
			return "René Descartes";
		}

		@Override
		public String largeImageUrl() {
			return "http://javaone.com/keynote_large.jpg";
		}

		@Override
		public String mediaUrl() {
			return "http://javaone.com/keynote.mpg";
		}

		@Override
		public String secondPerson() {
			return "Steve Jobs";
		}

		@Override
		public String smallImageUrl() {
			return "http://javaone.com/keynote_thumbnail.jpg";
		}

		@Override
		public String tag() {
			return "Javaone Keynotè";
		}
	}

	TextSource ASCII = new Ascii();

	TextSource MIXED = new Mixed();

	String firstPerson();

	String largeImageUrl();

	String mediaUrl();

	String secondPerson();

	String smallImageUrl();

	String tag();

}
