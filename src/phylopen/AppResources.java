/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import com.google.gson.JsonObject;

/**
 *
 * @author Work
 */
public class AppResources
{
    private static MainWindowController controller;
    
    public static MainWindowController getController()
    {
        return controller;
    }
    
    public static void setController(MainWindowController contr)
    {
        controller = contr;
    }
    
    public static PhyloPenOptions getOptions()
    {
        return controller.getOptions();
    }
}
