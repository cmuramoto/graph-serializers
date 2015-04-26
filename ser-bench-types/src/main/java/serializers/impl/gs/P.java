package serializers.impl.gs;

import java.io.Closeable;
import java.io.IOException;

import com.nc.gs.core.Context;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class P implements Closeable {

	public final Context ctx;
	public final Sink dst;
	public final Source src;

	public P() {
		this.dst = new Sink(4096);
		this.src = new Source(4096);
		ctx = Context.writing();
	}

	@Override
	public void close() throws IOException {
		dst.clear();
		src.clear();
		ctx.clear();
	}

	public Context reading() {
		return ctx.asReading();
	}

	public Context writing() {
		return ctx.asWriting();
	}
}