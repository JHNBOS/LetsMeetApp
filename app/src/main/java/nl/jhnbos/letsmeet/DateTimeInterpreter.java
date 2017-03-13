package nl.jhnbos.letsmeet;

import java.util.Calendar;

/**
 * Created by Johan Bos on 12-3-2017.
 */

public interface DateTimeInterpreter {
    String interpretDate(Calendar date);
    String interpretTime(int hour);
}
