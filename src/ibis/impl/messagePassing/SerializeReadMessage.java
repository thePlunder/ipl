package ibis.ipl.impl.messagePassing;

import java.io.ObjectInput;

import ibis.ipl.IbisIOException;

final class SerializeReadMessage extends ibis.ipl.impl.messagePassing.ReadMessage {

    java.io.ObjectInput obj_in;

    SerializeReadMessage(ibis.ipl.SendPort origin,
			 ReceivePort port) {
	super(origin, port);
	if (ibis.ipl.impl.messagePassing.Ibis.DEBUG) {
	    System.err.println("~~~~~~~~~ A new -sun- ReadMessage " + this);
	}
	obj_in = ((SerializeShadowSendPort)origin).obj_in;
    }

    public boolean readBoolean() throws IbisIOException {
	try {
	    return obj_in.readBoolean();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	}
    }

    public byte readByte() throws IbisIOException {
	try {
	    return (byte) obj_in.read();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	}
    }

    public char readChar() throws IbisIOException {
	try {
	    return obj_in.readChar();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	}
    }

    public short readShort() throws IbisIOException {
	try {
	    return obj_in.readShort();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	}
    }

    public int  readInt() throws IbisIOException {
	try {
	    return obj_in.readInt();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	}
    }

    public long readLong() throws IbisIOException {
	try {
	    return obj_in.readLong();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	}
    }

    public float readFloat() throws IbisIOException {
	try {
	    return obj_in.readFloat();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	}
    }

    public double readDouble() throws IbisIOException {
	try {
	    return obj_in.readDouble();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	}
    }

    public String readString() throws IbisIOException {
	try {
	    return (String) obj_in.readObject();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public Object readObject() throws IbisIOException {
// System.err.println("SerializeReadMessage.readObject() called " + this);
	try {
	    return obj_in.readObject();
	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readArrayBoolean(boolean[] destination) throws IbisIOException {
	try {
	    boolean[] temp = (boolean[])obj_in.readObject();
	    if (temp.length != destination.length) {
		throw new IbisIOException("Destination has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, 0, temp.length);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readArrayByte(byte[] destination) throws IbisIOException {
	try {
	    byte[] temp = (byte[])obj_in.readObject();
	    if (temp.length != destination.length) {
		throw new IbisIOException("Destination has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, 0, temp.length);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readArrayChar(char[] destination) throws IbisIOException {
	try {
	    char[] temp = (char[]) obj_in.readObject();
	    if (temp.length != destination.length) {
		throw new IbisIOException("Destination has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, 0, temp.length);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readArrayShort(short[] destination) throws IbisIOException {

	try {
	    short[] temp = (short[]) obj_in.readObject();
	    if (temp.length != destination.length) {
		throw new IbisIOException("Destination has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, 0, temp.length);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readArrayInt(int[] destination) throws IbisIOException {
	// manta.runtime.RuntimeSystem.DebugMe(this, destination);
	try {
	    int[] temp = (int[]) obj_in.readObject();
	    // manta.runtime.RuntimeSystem.DebugMe(temp, destination);
	    if (temp.length != destination.length) {
		throw new IbisIOException("Destination has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, 0, temp.length);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readArrayLong(long[] destination) throws IbisIOException {
	try {
	    long[] temp = (long[]) obj_in.readObject();
	    if (temp.length != destination.length) {
		throw new IbisIOException("Destination has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, 0, temp.length);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readArrayFloat(float[] destination) throws IbisIOException {
	try {
	    float[] temp = (float[]) obj_in.readObject();
	    if (temp.length != destination.length) {
		throw new IbisIOException("Destination has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, 0, temp.length);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readArrayDouble(double[] destination) throws IbisIOException {
	try {
	    double[] temp = (double[]) obj_in.readObject();
	    if (temp.length != destination.length) {
		throw new IbisIOException("Destination has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, 0, temp.length);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readSubArrayBoolean(boolean[] destination, int offset,
				    int size) throws IbisIOException {
	try {
	    boolean[] temp = (boolean[])obj_in.readObject();
	    if (temp.length != size) {
		throw new IbisIOException("Received sub array has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, offset, size);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readSubArrayByte(byte[] destination, int offset,
				 int size) throws IbisIOException {
	try {
	    byte[] temp = (byte[])obj_in.readObject();
	    if (temp.length != size) {
		throw new IbisIOException("Received sub array has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, offset, size);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

    public void readSubArrayChar(char[] destination, int offset,
				 int size) throws IbisIOException {

	try {
	    char[] temp = (char[]) obj_in.readObject();
	    if (temp.length != size) {
		throw new IbisIOException("Received sub array has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, offset, size);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}

    }

    public void readSubArrayShort(short[] destination, int offset,
				  int size) throws IbisIOException {

	try {
	    short[] temp = (short[]) obj_in.readObject();
	    if (temp.length != size) {
		throw new IbisIOException("Received sub array has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, offset, size);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}

    }

    public void readSubArrayInt(int[] destination, int offset,
				int size) throws IbisIOException {

	try {
	    int[] temp = (int[]) obj_in.readObject();
	    if (temp.length != size) {
		throw new IbisIOException("Received sub array has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, offset, size);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}

    }

    public void readSubArrayLong(long[] destination, int offset,
				 int size) throws IbisIOException {
	try {
	    long[] temp = (long[]) obj_in.readObject();
	    if (temp.length != size) {
		throw new IbisIOException("Received sub array has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, offset, size);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}

    }

    public void readSubArrayFloat(float[] destination, int offset,
				  int size) throws IbisIOException {

	try {
	    float[] temp = (float[]) obj_in.readObject();
	    if (temp.length != size) {
		throw new IbisIOException("Received sub array has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, offset, size);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}

    }

    public void readSubArrayDouble(double[] destination, int offset,
				   int size) throws IbisIOException {
	try {
	    double[] temp = (double[]) obj_in.readObject();
	    if (temp.length != size) {
		throw new IbisIOException("Received sub array has wrong size");
	    }
	    System.arraycopy(temp, 0, destination, offset, size);

	} catch (java.io.IOException e) {
	    throw new IbisIOException(e);
	} catch (ClassCastException e2) {
	    throw new IbisIOException("reading wrong type in stream", e2);
	} catch (ClassNotFoundException e3) {
	    throw new IbisIOException("class not found" + e3);
	}
    }

}
