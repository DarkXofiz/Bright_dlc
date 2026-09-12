package exloran.bright;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu listesinde "Bright" satirinin dislisine tiklaninca
 * dogrudan bizim ClickGUI'mizi (BrightMenu) acar.
 * Baska hicbir yerde (tus vb.) bu ekran acilmaz.
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new Bright.BrightMenu();
    }
}
