package io.klib.eclipse.snippets;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.service.command.Descriptor;
import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Component;

//@formatter:off
@Component(
	property = { 
		"osgi.command.scope=zEclipseSnippets",
		"osgi.command.function=listSnippets", 
		"osgi.command.function=launchSnippet" 
	}, service = EclipseSnippetLauncher.class)
//@formatter:on
@ConsumerType
public class EclipseSnippetLauncher {

	public final static String SNIPPET_PAGE = "https://www.eclipse.org/swt/snippets/";
	private String[] snippetPage;

	@Descriptor("usage guide")
	public void launch() {
		System.out.println("usage: launch <snippet_nr>");
	}

	@Descriptor("Launch Eclipse Snippet")
	public void launchSnippet(String number) {
		String snippetClass = "org.eclipse.swt.snippets.Snippet" + number;
		System.out.println("executing EclipseSnippet " + snippetClass);
		try {
			Class<?> clazz = Class.forName(snippetClass);
			Method mainMethod = clazz.getDeclaredMethod("main", String[].class);
			mainMethod.invoke(null, (Object) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Kill++ Eclipse Snippet")
	public void killSnippet(String number) {
		String snippetClass = "org.eclipse.swt.snippets.Snippet" + number;
		System.out.println("executing EclipseSnippet " + snippetClass);
		try {
			Class<?> clazz = Class.forName(snippetClass);
			Field field = clazz.getDeclaredField("myField");
			field.setAccessible(true);
			field.set(null, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Launch Eclipse Snippet")
	public void listSnippets() {
		String packageName = "org.eclipse.swt.snippets";
		System.out.println("following Eclipse Snippets are available:");
		Bundle bundle = FrameworkUtil.getBundle(EclipseSnippetLauncher.class);
		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		Collection<String> classes = bundleWiring.listResources(packageName.replace('.', '/'), "*.class",
				BundleWiring.LISTRESOURCES_LOCAL);
		for (String classname : classes) {
			if (!classname.contains("$")) {
				System.out.format("%5s - %s\n", classname.replaceFirst(".*Snippet(\\d+).*", "$1"),
						readComment(classname.replaceFirst(".*/", "").replace(".class", "")));
			}
		}
	}

	private String readComment(String patternString) {
		Pattern pattern = Pattern.compile(patternString);
		for (String line : readSnippetPage()) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				// Print the matched line
				return line.replaceFirst(".*.java\">(.*?)</a>.*", "$1");
			}
		}
		return "no description found";
	}

	private String[] readSnippetPage() {

		if (snippetPage == null) {
			try {
				HttpClient client = HttpClient.newHttpClient();
				HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SNIPPET_PAGE)).build();
				HttpResponse<String> response = client.send(request,
						HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

				// Process the response body line by line
				String responseBody = response.body();

				snippetPage = responseBody.split("\\r?\\n");
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return snippetPage;
	}
}