//
// Decompiled by Jadx - 573ms
//
package com.aide.ui.services;

import abcd.hy;
import android.app.Activity;
import com.aide.ui.App;
import com.aide.ui.util.BuildGradle;
import com.aide.ui.util.MavenMetadataXml;
import com.aide.ui.util.PomXml;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;


class NativeCodeSupportService$q implements Callable<Void> {
    private Runnable DW;
    private List<BuildGradle.MavenDependency> FH;
    private List<BuildGradle.RemoteRepository> Hw;
    private Activity j6;
    @hy
    final NativeCodeSupportService v5;

    public NativeCodeSupportService$q(NativeCodeSupportService nativeCodeSupportService, Activity activity, List<BuildGradle.MavenDependency> list, List<BuildGradle.RemoteRepository> list2, Runnable runnable) {
        try {
            this.v5 = nativeCodeSupportService;
            this.j6 = activity;
            this.DW = runnable;
            this.FH = list;
            this.Hw = list2;
        }
		catch (Throwable th) {

            throw new Error(th);
        }
    }

    public static Runnable j6(NativeCodeSupportService$q nativeCodeSupportService$q) {
        return nativeCodeSupportService$q.DW;
    }
	
	private static final BuildGradle.RemoteRepository defaultRemoteRepository = new BuildGradle.RemoteRepository(1, "https://maven.aliyun.com/repository/public");
    @Override
    public Void call() {

		ArrayList<BuildGradle.RemoteRepository> remoteRepositorys = new ArrayList<>();
		{
			Set<BuildGradle.RemoteRepository> remoteRepositorySet = new HashSet<>();
			remoteRepositorySet.add(defaultRemoteRepository);
			for (BuildGradle.RemoteRepository remoteRepository : this.Hw) {
				if (!remoteRepositorySet.contains(remoteRepository)) {
					remoteRepositorySet.add(remoteRepository);
					remoteRepositorys.add(remoteRepository);
				}
			}
		}
		//是否有已完成的下载
		boolean complete = false;
		int count = 0;
		for (BuildGradle.MavenDependency dependency : this.FH) {
			try {
				//遍历远程仓库
				for (BuildGradle.RemoteRepository remoteRepository : remoteRepositorys) {
					String mavenMetadataUrl = MavenService.rN(remoteRepository, dependency);
					String mavenMetadataPath = MavenService.lg(remoteRepository, dependency);
					try {
						//已存在 长度不一致时更新
						NativeCodeSupportService.gn(this.v5, mavenMetadataUrl, mavenMetadataPath, false);
					}
					catch (Exception unused) {
					}
					if (!new File(mavenMetadataPath).exists()) {
						continue;
					}
					MavenMetadataXml metadataXml = new MavenMetadataXml().getConfiguration(mavenMetadataPath);

					//查看maven-metadata.xml是否下载成功
					String version;
					if (new File(mavenMetadataPath).exists() 
						&& (version = metadataXml.getVersion(dependency.version)) != null) {
						String pomUrl = MavenService.getArtifactUrl(remoteRepository, dependency, version, ".pom");
						String pomPath = MavenService.getArtifactPath(remoteRepository, dependency, version, ".pom");

						//pom不存在，下载pom文件
						File pomFile = new File(pomPath);
						if (!pomFile.exists()) {
							try {
								NativeCodeSupportService.gn(this.v5, pomUrl, pomPath, true);
							}
							catch (Exception unused) {
							}
						}
						if (!pomFile.exists()) {
							//仓库有问题，跳过
							continue;
						}


						final String packaging = PomXml.empty.getConfiguration(pomPath).packaging;
						//更新依赖库packaging
						//双重验证
						if ("pom".equals(packaging)
							&& "pom".equals(dependency.packaging)) {
							count++;
							complete = true;
							//无论成功与否都当做bom
							break;

						}
						dependency.packaging = packaging;
						String artifactType = "." + packaging;

						//下载
						if (downloadArtifactFile(remoteRepository, dependency, version, artifactType, count)) {
							count++;
							complete = true;
							break;
						}

					}

				}
			}
			catch (Throwable e) {

			}
		}
		App.aj(new NativeCodeSupportService$q$a(this, complete));
		return null;
	}

	public boolean downloadArtifactFile(BuildGradle.RemoteRepository remoteRepository, BuildGradle.MavenDependency dependency, String version, String artifactType, int count) {
		String artifactUrl = MavenService.aM(remoteRepository, dependency, version, artifactType);
		String artifactPath = MavenService.XL(remoteRepository, dependency, version, artifactType);
		File artifactFile = new File(artifactPath);

		if (!artifactFile.exists()) {
			StringBuilder sb = new StringBuilder();
			sb.append(dependency.groupId);
			sb.append(":");
			sb.append(dependency.artifactId);
			sb.append(":");
			sb.append(version);
			sb.append("@");
			sb.append(dependency.packaging);

			String dependencyString = sb.toString();
			//通知下载进度
			NativeCodeSupportService.Hw(this.v5, dependencyString, (count * 100) / this.FH.size(), 0);
			try {
				//如果文件存在且长度一致则不下载
				NativeCodeSupportService.gn(this.v5, artifactUrl, artifactPath, true);
			}
			catch (Exception unused) {}
		}

		return artifactFile.exists();
	}

    public Void call3() {
        String buildMavenMetadataPath = null;
        String version;
        String aarPath;
        String jarUrl;
        String jarPath;
        String jarType = ".jar";
        String aarType = ".aar";
        String pomType = ".pom";
        try {

            ArrayList<BuildGradle.RemoteRepository> remoteRepositorys = new ArrayList<>();

            for (BuildGradle.RemoteRepository remoteRepository : this.Hw) {
                if (!remoteRepositorys.contains(remoteRepository)) {
                    remoteRepositorys.add(remoteRepository);
                }
            }

            Iterator<BuildGradle.MavenDependency> it = this.FH.iterator();
            boolean z2 = false;
            int i = 0;
            while (it.hasNext()) {

                if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				BuildGradle.MavenDependency dependency = it.next();
				for (BuildGradle.RemoteRepository remoteRepository : remoteRepositorys) {
					try {
						String buildMavenMetadataUrl = MavenService.rN(remoteRepository, dependency);
						buildMavenMetadataPath = MavenService.lg(remoteRepository, dependency);
						//下载
						NativeCodeSupportService.gn(this.v5, buildMavenMetadataUrl, buildMavenMetadataPath, false);
					}
					catch (Exception unused) {
					}
					// aM url
					// XL path
					if (new File(buildMavenMetadataPath).exists() && (version = new MavenMetadataXml().getConfiguration(buildMavenMetadataPath).getVersion(dependency.version)) != null) {
						String pomUrl = MavenService.aM(remoteRepository, dependency, version, pomType);
						String pomPath = MavenService.XL(remoteRepository, dependency, version, pomType);

						String aarUrl = MavenService.aM(remoteRepository, dependency, version, aarType);
						aarPath = MavenService.XL(remoteRepository, dependency, version, aarType);

						jarUrl = MavenService.aM(remoteRepository, dependency, version, jarType);
						jarPath = MavenService.XL(remoteRepository, dependency, version, jarType);


						if ((!new File(jarPath).exists() 
							&& !new File(aarPath).exists()) 

							|| !new File(pomPath).exists()) {

							StringBuilder sb = new StringBuilder();
							sb.append(dependency.groupId);
							sb.append(":");
							sb.append(dependency.artifactId);
							sb.append(":");
							sb.append(version);
							String dependencyString = sb.toString();

							//通知下载进度
							NativeCodeSupportService.Hw(this.v5, dependencyString, (i * 100) / this.FH.size(), 0);
							NativeCodeSupportService.gn(this.v5, pomUrl, pomPath, true);
							NativeCodeSupportService.gn(this.v5, aarUrl, aarPath, true);
							NativeCodeSupportService.gn(this.v5, jarUrl, jarPath, true);


							i++;
							if ((new File(jarPath).exists() 
								|| new File(aarPath).exists()) && new File(pomPath).exists()) {
								z2 = true;
								break;
							}
						}
					}
				}
            }
            App.aj(new NativeCodeSupportService$q$a(this, z2));
            return null;
        }
		catch (Throwable th) {
            throw new Error(th);
        }
    }
}

