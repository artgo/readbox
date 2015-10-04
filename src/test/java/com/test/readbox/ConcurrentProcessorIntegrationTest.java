package com.test.readbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.google.common.base.Stopwatch;
import com.google.inject.Guice;
import com.google.inject.Injector;

@Test(singleThreaded = true)
public class ConcurrentProcessorIntegrationTest {
	private static final long INTERFILE_DELAY = 1501L;
	private static final int TOTAL_FILES = 300;
	private static final int TOTAL_FILES_BIG = 6000;
	private static final String BOX_REFRESH_TOKEN = "BOX_REFRESH_TOKEN";
	private static final String BOX_ACCESS_TOKEN = "BOX_ACCESS_TOKEN";
	private static final String BOX_CLIENT_SECRET = "BOX_CLIENT_SECRET";
	private static final String BOX_CLIENT_ID = "BOX_CLIENT_ID";

	private BoxAPIConnection api;

	@BeforeClass(alwaysRun = true)
	public void setup() {
		String clientId = System.getenv(BOX_CLIENT_ID);
		String clientSecret = System.getenv(BOX_CLIENT_SECRET);
		String accessToken = System.getenv(BOX_ACCESS_TOKEN);
		String refreshToken = System.getenv(BOX_REFRESH_TOKEN);
		api = new BoxAPIConnection(clientId, clientSecret, accessToken, refreshToken);
		api.setLastRefresh(System.currentTimeMillis());
		api.setExpires(3_600_000);
		//api.setExpires(100);
		Reporter.log("API init complete", true);
	}

	@AfterClass(alwaysRun = true)
	public void after() {
		Reporter.log("access: " + api.getAccessToken(), true);
		Reporter.log("refresh: " + api.getRefreshToken(), true);
	}

	public void test_picks_150_first_from_subdir() throws Exception {
		Stopwatch stopwatch = Stopwatch.createStarted();
		Reporter.log("Start 150 test", true);
		//upload_300_files_to_a_folder();
		//upload_6000_files_to_35_folders();
		try {
			ConcurrentProcessor processor = getProcessor();
			Collection<FileInfo> results = processor.getResults();
			assertThat(results).isNotEmpty();
			assertThat(results).hasSize(150);
			FileInfo prev = null;
			for (FileInfo fileInfo : results) {
				if (prev != null) {
					int prevIndex = indexFormName(prev.getName());
					int curIndex = indexFormName(fileInfo.getName());
					assertThat(curIndex).isLessThan(prevIndex);
				}
				prev = fileInfo;
			}
		} finally {
			stopwatch.stop();
			Reporter.log("Test complete in " + stopwatch, true);
		}
	}

	private int indexFormName(String name) {
		return Integer.parseInt(StringUtils.substring(name, 1));
	}

	public void test_picks_150_first_from_subdir_100_times() throws Exception {
		long total = 0;
		//ConcurrentProcessor processor = getProcessor();
		for (int i = 0; i < 100; i++) {
			long init = System.currentTimeMillis();
			//processor.getResults();
			long diff = System.currentTimeMillis() - init;
			total += diff;
			//Reporter.log("Test took " + diff + " ms", true);
		}
		Reporter.log("Average: " + (total / 100.0), true);
	}

	protected void upload_300_files_to_a_folder() throws Exception {
		BoxFolder root = BoxFolder.getRootFolder(api);
		Reporter.log("Cleaninig...", true);
		cleanup(api, root);
		Reporter.log("Cleanup complete. Uploading " + TOTAL_FILES + " files", true);
		Thread.sleep(INTERFILE_DELAY);
		BoxFolder.Info folderInfo = root.createFolder("tst2");
		BoxFolder folder = new BoxFolder(api, folderInfo.getID());
		Reporter.log("Folder created", true);
		for (int i = 0; i < TOTAL_FILES; i++) {
			InputStream is = IOUtils.toInputStream("text" + i);
			folder.uploadFile(is, "f" + i);
			Reporter.log("File f" + i + " uploaded", true);
			Thread.sleep(INTERFILE_DELAY);
		}
		Reporter.log("Upload complete", true);
	}

	protected void upload_6000_files_to_35_folders() throws Exception {
		BoxFolder root = BoxFolder.getRootFolder(api);
		Reporter.log("Cleaninig...", true);
		cleanup(api, root);
		Thread.sleep(INTERFILE_DELAY);

		Reporter.log("Cleanup complete. Creating folders...", true);

		ArrayList<BoxFolder> folders = new ArrayList<>();
		folders.add(root);
		createFolder(root, "fl1", folders);
		BoxFolder fl2 = createFolder(root, "fl2", folders);
		BoxFolder fl3 = createFolder(root, "fl3", folders);
		BoxFolder fl4 = createFolder(root, "fl4", folders);
		BoxFolder fl5 = createFolder(root, "fl5", folders);
		BoxFolder fl6 = createFolder(root, "fl6", folders);
		BoxFolder fl7 = createFolder(root, "fl7", folders);

		createFolder(fl2, "fl21", folders);

		createFolder(fl3, "fl31", folders);
		createFolder(fl3, "fl32", folders);

		BoxFolder fl41 = createFolder(fl4, "fl41", folders);
		BoxFolder fl411 = createFolder(fl41, "fl411", folders);
		createFolder(fl411, "fl4111", folders);

		createFolder(fl5, "fl51", folders);
		BoxFolder fl52 = createFolder(fl5, "fl52", folders);
		createFolder(fl52, "fl521", folders);
		createFolder(fl52, "fl522", folders);
		BoxFolder fl523 = createFolder(fl52, "fl523", folders);
		createFolder(fl523, "fl5231", folders);

		createFolder(fl6, "fl61", folders);
		BoxFolder fl62 = createFolder(fl6, "fl62", folders);
		createFolder(fl62, "fl621", folders);
		BoxFolder fl622 = createFolder(fl62, "fl622", folders);
		createFolder(fl622, "fl6221", folders);
		BoxFolder fl6222 = createFolder(fl622, "fl6222", folders);
		createFolder(fl6222, "fl62221", folders);
		createFolder(fl6222, "fl62222", folders);

		BoxFolder fl71 = createFolder(fl7, "fl71", folders);
		BoxFolder fl711 = createFolder(fl71, "fl711", folders);
		BoxFolder fl7111 = createFolder(fl711, "fl7111", folders);
		BoxFolder fl71111 = createFolder(fl7111, "fl71111", folders);
		BoxFolder fl711111 = createFolder(fl71111, "fl711111", folders);
		BoxFolder fl7111111 = createFolder(fl711111, "fl7111111", folders);
		createFolder(fl7111111, "fl71111111", folders);

		Reporter.log("" + folders.size() + " folder created. Uploading " + TOTAL_FILES_BIG + " files", true);

		Random random = new Random();
		for (int i = 0; i < TOTAL_FILES_BIG; i++) {
			InputStream is = IOUtils.toInputStream("text" + i);
			int randomIndex = random.nextInt(folders.size());
			BoxFolder folder = folders.get(randomIndex);
			try {
				folder.uploadFile(is, "f" + i);
				Reporter.log("File f" + i + " uploaded", true);
				Thread.sleep(INTERFILE_DELAY);
			} catch (Exception e) {
				Reporter.log("f" + i + " upload failed", true);
			}
		}

		Reporter.log("Upload complete.", true);
	}

	private BoxFolder createFolder(BoxFolder parent, String name, ArrayList<BoxFolder> folders) {
		try {
			BoxFolder.Info folderInfo = parent.createFolder(name);
			BoxFolder folder = new BoxFolder(api, folderInfo.getID());
			folders.add(folder);
			Reporter.log("Folder " + name + " created", true);
			return folder;
		} catch (Exception e) {
			Reporter.log("Folder " + name + " failed to be created", true);
			return parent;
		}
	}

	private void cleanup(BoxAPIConnection api, BoxFolder root) {
		for (BoxItem.Info info : root) {
		    if (info instanceof BoxFolder.Info) {
		    	BoxFolder f = new BoxFolder(api, info.getID());
		    	f.delete(true);
		    } else {
		    	BoxFile f = new BoxFile(api, info.getID());
		    	f.delete();
		    }
		}
	}

	private ConcurrentProcessor getProcessor() {
		Injector injector = Guice.createInjector(new BoxModule(api));
		ConcurrentProcessor processor = injector.getInstance(ConcurrentProcessor.class);
		return processor;
	}
}
