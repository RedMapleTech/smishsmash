package tech.redmaple.smishsmash.analysis;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import timber.log.Timber;

public class OrganisationInfo {

    private static final String JSON_FILE_NAME = "org_info.json";
    private String name;
    private String[] keywords;
    private String[] domains;

    public OrganisationInfo(String name, String[] keywords, String[] urls) {
        this.name = name;
        this.keywords = keywords;
        this.domains = urls;
    }

    public String[] getUrl() {
        return domains;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static OrganisationInfo[] getInfoFromJSON(Context context) {
        ArrayList<OrganisationInfo> arrayList = new ArrayList<>();
        String jsonString = "";

        try {
            InputStream is = context.getAssets().open(JSON_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonString.length() > 0) {
            Timber.d("getInfoFromJSON: Loaded JSON string from file");

            Gson gson = new Gson();
            Type listUserType = new TypeToken<List<OrganisationInfo>>() { }.getType();
            arrayList = gson.fromJson(jsonString, listUserType);

            Timber.i("getInfoFromJSON: Loaded OrgInfo for %d sets of organisations", arrayList.size());
        }

        OrganisationInfo[] infos = new OrganisationInfo[arrayList.size()];

        for (int i = 0; i < arrayList.size(); i++) {
            infos[i] = arrayList.get(i);
        }

        return infos;
    }
}
