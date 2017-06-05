package ua.gwm.sponge_plugin.crates.util;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import ua.gwm.sponge_plugin.crates.GWMCrates;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LanguageUtils {

    public static String getPhrase(String path, Pair<String, ?>... pairs) {
        ConfigurationNode node = GWMCrates.getInstance().getLanguageConfig().getNode(path.toUpperCase());
        try {
            String phrase = node.getValue(TypeToken.of(String.class), "ERROR! UNABLE TO GET PHRASE!");
            for (Pair<String, ?> pair : pairs) {
                phrase = phrase.replace(pair.getKey(), pair.getValue().toString());
            }
            return phrase;
        } catch (Exception e) {
            GWMCrates.getInstance().getLogger().warn("Failed on getting phrase \"" + path + "\" from language config!", e);
            return null;
        }
    }

    public static Text getText(String path, Pair<String, ?>... pairs) {
        try {
            return TextSerializers.FORMATTING_CODE.deserialize(getPhrase(path, pairs));
        } catch (Exception e) {
            GWMCrates.getInstance().getLogger().warn("Failed on getting text \"" + path + "\" from language config!", e);
            return null;
        }
    }

    public static List<String> getPhraseList(String path, Pair<String, ?>... pairs) {
        ConfigurationNode node = GWMCrates.getInstance().getLanguageConfig().getNode(path.toUpperCase());
        try {
            List<String> list = node.getValue(new TypeToken<List<String>>(){}, new ArrayList<String>());
            for (int i = 0; i < list.size(); i++) {
                String phrase = list.get(i);
                for (Pair<String, ?> pair : pairs) {
                    phrase = phrase.replace(pair.getKey(), pair.getValue().toString());
                }
                list.set(i, phrase);
            }
            return list;
        } catch (Exception e) {
            GWMCrates.getInstance().getLogger().warn("Failed on getting phrase list \"" + path + "\" from language config!", e);
            return null;
        }
    }

    public static List<Text> getTextList(String path, Pair<String, ?>... pairs) {
        try {
            return getPhraseList(path, pairs).stream().
                    map(TextSerializers.FORMATTING_CODE::deserialize).
                    collect(Collectors.toList());
        } catch (Exception e) {
            GWMCrates.getInstance().getLogger().warn("Failed on getting text list \"" + path + "\" from language config!", e);
            return null;
        }
    }
}
