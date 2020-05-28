/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class TooltipUtility
{
  /**
   * Returns true if successful.
   * Current defaults are 1000, 5000, 200;
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static boolean setTooltipTimers(long openDelay, long visibleDuration, long closeDelay)
  {
    try
    {
      Field f = Tooltip.class.getDeclaredField("BEHAVIOR");
      f.setAccessible(true);
      
      
      Class[] classes = Tooltip.class.getDeclaredClasses();
      for (Class clazz : classes)
      {
        if (clazz.getName().equals("javafx.scene.control.Tooltip$TooltipBehavior"))
        {
          Constructor ctor = clazz.getDeclaredConstructor(Duration.class, Duration.class, Duration.class, boolean.class);
          ctor.setAccessible(true);
          Object tooltipBehavior = ctor.newInstance(new Duration(openDelay), new Duration(visibleDuration), new Duration(closeDelay), false);
          f.set(null, tooltipBehavior);
          break;
        }
      }
    }
    catch (Exception e)
    {
      return false;
    }
    return true;
  }
}
