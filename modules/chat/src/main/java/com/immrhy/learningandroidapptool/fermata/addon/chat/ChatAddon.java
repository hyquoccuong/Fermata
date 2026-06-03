package com.immrhy.learningandroidapptool.fermata.addon.chat;

import static com.immrhy.learningandroidapptool.fermata.util.Utils.openUrl;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import java.util.Locale;

import com.immrhy.learningandroidapptool.fermata.FermataApplication;
import com.immrhy.learningandroidapptool.fermata.addon.AddonInfo;
import com.immrhy.learningandroidapptool.fermata.addon.FermataAddon;
import com.immrhy.learningandroidapptool.fermata.addon.FermataFragmentAddon;
import com.immrhy.learningandroidapptool.utils.app.App;
import com.immrhy.learningandroidapptool.utils.function.IntSupplier;
import com.immrhy.learningandroidapptool.utils.function.Supplier;
import com.immrhy.learningandroidapptool.utils.misc.ChangeableCondition;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceSet;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore.Pref;
import com.immrhy.learningandroidapptool.utils.ui.fragment.ActivityFragment;

/**
 * @author Andrey Pavlenko
 */
@Keep
@SuppressWarnings("unused")
public class ChatAddon implements FermataFragmentAddon {
	private static final AddonInfo info = FermataAddon.findAddonInfo(ChatAddon.class.getName());
	private static final Pref<Supplier<String>> OPENAI_KEY = Pref.s("OPENAI_KEY", "");
	private static final String[] MODELS =
			new String[]{"gpt-4o", "chatgpt-4o-latest", "gpt-4o-mini", "o1", "o1-mini", "o1-preview"};
	private static final Pref<IntSupplier> MODEL = Pref.i("MODEL", 2);
	private static final Pref<Supplier<String>> MODEL_OTHER = Pref.s("MODEL_OTHER", "");
	private static final Pref<Supplier<String>> CHAT_LANG =
			Pref.s("CHAT_LANG", () -> Locale.getDefault().toLanguageTag());

	@Override
	public int getAddonId() {
		return com.immrhy.learningandroidapptool.fermata.R.id.chat_addon;
	}

	@NonNull
	@Override
	public AddonInfo getInfo() {
		return info;
	}

	@NonNull
	@Override
	public ActivityFragment createFragment() {
		return new ChatFragment();
	}

	public String getOpenaiKey() {
		return FermataApplication.get().getPreferenceStore().getStringPref(OPENAI_KEY);
	}

	public String getModel() {
		var ps = FermataApplication.get().getPreferenceStore();
		var other = ps.getStringPref(MODEL_OTHER).trim();
		if (!other.isEmpty()) return other;
		int i = ps.getIntPref(MODEL);
		return (i >= 0) && (i < MODELS.length) ? MODELS[i] :
				MODELS[MODEL.getDefaultValue().getAsInt()];
	}

	public String getGetChatLang() {
		return FermataApplication.get().getPreferenceStore().getStringPref(CHAT_LANG);
	}

	@Override
	public void contributeSettings(Context ctx, PreferenceStore store, PreferenceSet set,
																 ChangeableCondition visibility) {
		set.addStringPref(o -> {
			String keyUrl = "https://platform.openai.com/api-keys";
			String sub = App.get().getString(R.string.openai_key_sub, keyUrl);
			o.store = store;
			o.pref = OPENAI_KEY;
			o.title = R.string.openai_key;
			o.csubtitle = HtmlCompat.fromHtml(sub, HtmlCompat.FROM_HTML_MODE_COMPACT);
			o.clickListener = v -> openUrl(v.getContext(), keyUrl);
		});
		set.addListPref(o -> {
			o.store = store;
			o.pref = MODEL;
			o.title = R.string.openai_model;
			o.stringValues = MODELS;
			o.subtitle = com.immrhy.learningandroidapptool.fermata.R.string.string_format;
			o.formatSubtitle = true;
		});
		set.addStringPref(o -> {
			o.store = store;
			o.pref = MODEL_OTHER;
			o.title = R.string.openai_model_other;
			o.stringHint = "gpt-4-turbo";
		});
		set.addTtsLocalePref(o -> {
			o.store = store;
			o.pref = CHAT_LANG;
			o.title = com.immrhy.learningandroidapptool.fermata.R.string.lang;
			o.subtitle = com.immrhy.learningandroidapptool.fermata.R.string.string_format;
			o.formatSubtitle = true;
		});
	}
}
