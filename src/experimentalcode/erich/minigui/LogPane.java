package experimentalcode.erich.minigui;

import java.awt.Color;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import de.lmu.ifi.dbs.elki.logging.LoggingConfiguration;
import de.lmu.ifi.dbs.elki.logging.MessageFormatter;

/**
 * A Swing object to receive ELKI logging output.
 * Call {@link #becomeDefaultLogger()} to register as default logger in ELKI.
 * 
 * @author Erich Schubert
 */
public class LogPane extends JTextPane {
  /**
   * Serialization version number
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * Base (default) style
   */
  protected Style baseStyle;
  
  /**
   * Regular message style
   */
  protected Style msgStyle;
  
  /**
   * Debug message style
   */
  protected Style dbgStyle;

  /**
   * Error message style
   */
  protected Style errStyle;

  /**
   * Formatter for regular messages (informational records)
   */
  private Formatter msgformat = new MessageFormatter();

  /**
   * Formatter for debugging messages
   */
  private Formatter debugformat = new SimpleFormatter();

  /**
   * Formatter for error messages
   */
  private Formatter errformat = new SimpleFormatter();
  
  /**
   * Constructor
   */
  public LogPane() {
    super();
    // setup styles
    baseStyle = getStyledDocument().addStyle(null, null);
    msgStyle = getStyledDocument().addStyle("msg", baseStyle);
    errStyle = getStyledDocument().addStyle("err", baseStyle);
    errStyle.addAttribute(StyleConstants.Foreground, Color.RED);
    dbgStyle = getStyledDocument().addStyle("err", baseStyle);
    dbgStyle.addAttribute(StyleConstants.Foreground, Color.BLUE);
  }

  /**
   * Print a message as if it were logged, without going through the full logger.
   * 
   * @param message Message text
   * @param level Message level
   */
  public void publish(String message, Level level) {
    try {
      publish(new LogRecord(level, message));
    }
    catch(Exception e) {
      throw new RuntimeException("Error writing a log-like message.", e);
    }
  }

  /**
   * Publish a log record to the logging pane.
   * 
   * @param record Log record
   * @throws Exception
   */
  protected void publish(LogRecord record) throws Exception {
    // choose an appropriate formatter
    final Formatter fmt;
    final Style style;
    // always format progress messages using the progress formatter.
    if(record.getLevel().intValue() >= Level.WARNING.intValue()) {
      // format errors using the error formatter
      fmt = errformat;
      style = errStyle;
    }
    else if(record.getLevel().intValue() <= Level.FINE.intValue()) {
      // format debug statements using the debug formatter.
      fmt = debugformat;
      style = dbgStyle;
    }
    else {
      // default to the message formatter.
      fmt = msgformat;
      style = msgStyle;
    }
    // format
    final String m;
    m = fmt.format(record);
    getStyledDocument().insertString(getStyledDocument().getLength(), m, style);
  }  
  
  /**
   * Clear the current contents.
   */
  public void clear() {
    setText("");
  }
  
  /**
   * Become the default logger.
   */
  public void becomeDefaultLogger() {
    LoggingConfiguration.replaceDefaultHandler(new LogPaneHandler());
  }
  
  /**
   * Internal Handler to insert into {@link java.util.logging}
   * 
   * @author Erich Schubert
   */
  private class LogPaneHandler extends Handler {
    /**
     * Constructor.
     */
    protected LogPaneHandler() {
      super();
    }

    @Override
    public void close() throws SecurityException {
      // do nothing
    }

    @Override
    public void flush() {
      // do nothing
    }

    @Override
    public void publish(LogRecord record) {
      try {
        LogPane.this.publish(record);
      }
      catch(Exception e) {
        reportError("Error printing output log message.", e, ErrorManager.GENERIC_FAILURE);
      }
    }
  }
}
