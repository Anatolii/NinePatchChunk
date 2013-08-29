package ua.anatolii.graphics.ninepatch;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
* Created by Anatolii on 8/27/13.
*/
public final class Div implements Externalizable {
	int start;
	int stop;

	public Div() {

	}

	public Div(int start, int stop) {
		this.start = start;
		this.stop = stop;
	}

	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		start = input.readByte();
		stop = input.readByte();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(start);
		output.writeByte(stop);
	}
}
