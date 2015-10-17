/*
 * Complex implementation to java
 */
package gsm.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author bastienjalbert
 */
public class Complex implements Serializable{
    private float real;
    private float imaginary;

    public Complex()
    {
        this( 0, 0 );
    }

    public String toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        out = new ObjectOutputStream(bos);   
        out.writeObject(this);
        byte[] yourBytes = bos.toByteArray();
        String retourne = "";
        for(int i = 0; i < yourBytes.length ; i++) {
            retourne += yourBytes[i];
        }
        return retourne;
    }

    public Complex( float r, float i )
    {
        real = r;
        imaginary = i;
    }

    public double getReal() {
      return this.real;
    }

    public double getImaginary() {
      return this.imaginary;
    }
}