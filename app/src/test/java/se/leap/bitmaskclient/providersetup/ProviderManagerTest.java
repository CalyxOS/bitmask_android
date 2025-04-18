package se.leap.bitmaskclient.providersetup;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import mobile.BitmaskMobile;
import mobilemodels.BitmaskMobileCore;
import se.leap.bitmaskclient.base.models.Constants;
import se.leap.bitmaskclient.base.models.Provider;
import se.leap.bitmaskclient.base.utils.BitmaskCoreProvider;
import se.leap.bitmaskclient.base.utils.PreferenceHelper;
import se.leap.bitmaskclient.testutils.MockHelper;
import se.leap.bitmaskclient.testutils.MockSharedPreferences;
import se.leap.bitmaskclient.testutils.TestSetupHelper;

/**
 * Created by cyberta on 20.02.18.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class ProviderManagerTest {

    private AssetManager assetManager;
    private ProviderManager providerManager;
    PreferenceHelper preferenceHelper;
    SharedPreferences mockSharedPrefs;
    MockHelper.MockFileHelper mockedFileHelperInterface;

    @Before
    public void setup() throws Exception {
        assetManager = mock(AssetManager.class);
        BitmaskCoreProvider.initBitmaskMobile(TestSetupHelper.getCustomBitmaskCore());

        when(assetManager.open(anyString())).thenAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                String filename = "preconfigured/" + invocation.getArguments()[0];
                return getClass().getClassLoader().getResourceAsStream(filename);
            }
        });
        when(assetManager.list(anyString())).thenAnswer(new Answer<String[]>() {
            @Override
            public String[] answer(InvocationOnMock invocation) throws Throwable {
                String path = (String) invocation.getArguments()[0];
                if ("urls".equals(path)) {
                    String[] preconfiguredUrls = new String[3];
                    preconfiguredUrls[0] = "calyx.net.url";
                    preconfiguredUrls[1] = "demo.bitmask.net.url";
                    preconfiguredUrls[2] = "riseup.net.url";
                    return preconfiguredUrls;
                } else
                    throw new IllegalArgumentException("You need to implement the expected path manually!");
            }
        });

        mockSharedPrefs = new MockSharedPreferences();
        preferenceHelper = new PreferenceHelper(mockSharedPrefs);

        HashSet<Provider> customProviders = new HashSet<>();
        customProviders.add(Provider.createCustomProvider("https://leapcolombia.org", "leapcolombia.org", null));
        PreferenceHelper.setCustomProviders(customProviders);
    }

    @After
    public void tearDown() {
        ProviderManager.reset();
    }

    @Test
    public void testSize_dummyEntry_has5ProvidersWithCurrentTestSetup() {
        providerManager = ProviderManager.getInstance(assetManager);
        providerManager.setAddDummyEntry(true);
        assertEquals("3 preconfigured, 1 custom provider, 1 dummy provider", 5, providerManager.size());
    }

    @Test
    public void testSize_has4ProvidersWithCurrentTestSetup() {
        providerManager = ProviderManager.getInstance(assetManager);
        assertEquals("3 preconfigured, 1 custom provider", 4, providerManager.size());
    }


    @Test
    public void testAdd_dummyEntry_newCustomProviderThatIsNotPartOfDefaultNorCustomList_returnTrue() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        providerManager.setAddDummyEntry(true);
        Provider customProvider = new Provider("https://anewprovider.org");
        assertTrue("custom provider added: ", providerManager.add(customProvider));
        assertEquals("3 preconfigured, 2 custom providers, 1 dummy provider", 6, providerManager.providers().size());
    }

    @Test
    public void testAdd_newCustomProviderThatIsNotPartOfDefaultNorCustomList_returnTrue() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        Provider customProvider = new Provider("https://anewprovider.org");
        assertTrue("custom provider added: ", providerManager.add(customProvider));
        assertEquals("3 preconfigured, 2 custom providers", 5, providerManager.providers().size());
    }

    @Test
    public void testAdd_dummyEntry_newCustomProviderThatIsNotPartOfDefaultButOfCustomList_returnFalse() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        providerManager.setAddDummyEntry(true);
        Provider customProvider = new Provider("https://leapcolombia.org");
        assertFalse("custom provider added: ", providerManager.add(customProvider));
        assertEquals("3 preconfigured, 1 custom provider, 1 dummy provider", 5, providerManager.providers().size());
    }

    @Test
    public void testAdd_newCustomProviderThatIsNotPartOfDefaultButOfCustomList_returnFalse() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        Provider customProvider = new Provider("https://leapcolombia.org");
        assertFalse("custom provider added: ", providerManager.add(customProvider));
        assertEquals("3 preconfigured, 1 custom provider", 4, providerManager.providers().size());
    }

    @Test
    public void testAdd_newCustomProviderThatIsPartOfDefaultButNotOfCustomList_returnFalse() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        Provider customProvider = new Provider("https://demo.bitmask.net");
        assertFalse("custom provider added: ", providerManager.add(customProvider));
        assertEquals("3 preconfigured, 1 custom provider", 4, providerManager.providers().size());
    }

    @Test
    public void testRemove_ProviderIsPartOfDefaultButNotCustomList_returnsFalse() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        Provider customProvider = new Provider("https://demo.bitmask.net");
        assertFalse("custom provider not removed: ", providerManager.remove(customProvider));
        assertEquals("3 preconfigured, 1 custom provider", 4, providerManager.providers().size());
    }

    @Test
    public void testRemove_ProviderIsNotPartOfDefaultButOfCustomList_returnsTrue() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        Provider customProvider = new Provider("https://leapcolombia.org");
        assertTrue("custom provider not removed: ", providerManager.remove(customProvider));
        assertEquals("3 preconfigured, 0 custom providers", 3, providerManager.providers().size());
    }

    @Test
    public void testRemove_ProviderIsNotPartOfDefaultNorOfCustomList_returnsFalse() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        Provider customProvider = new Provider("https://anotherprovider.org");
        assertFalse("custom provider not removed: ", providerManager.remove(customProvider));
        assertEquals("3 preconfigured, 1 custom providers", 4, providerManager.providers().size());
    }

    @Test
    public void testClear_dummyEntry_ProvidersListHasOnlyDummyProvider() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        providerManager.setAddDummyEntry(true);
        providerManager.clear();
        assertEquals("1 providers", 1, providerManager.providers().size());
        assertEquals("provider is dummy element", "", providerManager.get(0).getMainUrl());
    }

    @Test
    public void testClear_noEntries() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        providerManager.clear();
        assertEquals("no providers", 0, providerManager.providers().size());
    }

    @Test
    public void testSaveCustomProvidersToFile_CustomProviderDeleted_deletesFromDir() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        //leapcolombia is mocked custom provider from setup
        Provider customProvider = new Provider("https://leapcolombia.org");
        providerManager.remove(customProvider);
        providerManager.saveCustomProviders();
        Set<String> providerSet = mockSharedPrefs.getStringSet(Constants.CUSTOM_PROVIDER_DOMAINS, new HashSet<>());
        assertEquals("provider was removed", 0, providerSet.size());
        assertEquals("preference helper has 0 custom providers", 0, PreferenceHelper.getCustomProviders().size());

    }


    @Test
    public void testSaveCustomProvidersToFile_newCustomProviders_persistNew() throws Exception {
        providerManager = ProviderManager.getInstance(assetManager);
        Provider customProvider = new Provider("https://anotherprovider.org");
        Provider secondCustomProvider = new Provider("https://yetanotherprovider.org");
        providerManager.add(customProvider);
        providerManager.add(secondCustomProvider);
        providerManager.saveCustomProviders();
        Set<String> providerSet = mockSharedPrefs.getStringSet(Constants.CUSTOM_PROVIDER_DOMAINS, new HashSet<>());
        assertEquals("persist was called twice", 3, providerSet.size());
        assertEquals("PreferenceHelper has 2 providers", 3, PreferenceHelper.getCustomProviders().size());

    }


}
