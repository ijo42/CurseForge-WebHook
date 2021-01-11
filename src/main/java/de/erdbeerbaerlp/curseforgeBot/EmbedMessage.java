package de.erdbeerbaerlp.curseforgeBot;

import com.github.rjeschke.txtmark.Processor;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.project.CurseProject;
import net.ranktw.DiscordWebHooks.DiscordEmbed;
import net.ranktw.DiscordWebHooks.DiscordMessage;
import net.ranktw.DiscordWebHooks.DiscordWebhook;
import net.ranktw.DiscordWebHooks.embed.*;

import java.util.Iterator;
import java.util.stream.Stream;


public class EmbedMessage {

	private static final String ZERO_WIDTH_SPACE = "";

	/**
	 * Message without link.
	 *
	 * @param proj    the proj
	 * @param file    the file
	 * @throws CurseException the curse exception
	 */
	public static void messageWithoutLink(CurseProject proj, CurseFile file, DiscordWebhook webhook)
			throws CurseException {
		DiscordEmbed embed = new DiscordEmbed.Builder().
				withTitle(proj.name() + proj.url().toString()).
				withThumbnail(new ThumbnailEmbed(proj.logo().thumbnailURL().toString(), 80, 80)).
				withDescription(getMessageDescription()).
				withField(new FieldEmbed(ZERO_WIDTH_SPACE,
				"**Release Type**: `" + file.releaseType().name() + "`" + "\n **File Name**: `" + file.displayName()
						+ "`" + "\n **Category**: `" + proj.categorySection().name() + "`" + "\n **GameVersion**: `"
						+ getGameVersions(proj) + "`", false)).
				withField(new FieldEmbed(ZERO_WIDTH_SPACE,
				"**Changelog:** \n```" + getSyntax() + "\n" + formatChangelog(file.changelogPlainText(1000)) + "\n```",
				false)).build();
		webhook.sendMessage(embed);
	}

	/**
	 * Message with curse link.
	 *
	 * @param proj    the proj
	 * @param file    the file
	 * @throws CurseException the curse exception
	 */
	public static void messageWithCurseLink(CurseProject proj, CurseFile file, DiscordWebhook webhook)
			throws CurseException {
		DiscordEmbed embed = new DiscordEmbed.Builder().
			withTitle(proj.name() + proj.url().toString()).
			withThumbnail(new ThumbnailEmbed(proj.logo().thumbnailURL().toString(), 80, 80)).
			withDescription(getMessageDescription()).
			withField(new FieldEmbed(ZERO_WIDTH_SPACE,
					"**Release Type**: `" + file.releaseType().name() + "`" + "\n **File Name**: `" + file.displayName()
							+ "`" + "\n **Category**: `" + proj.categorySection().name() + "`" + "\n **GameVersion**: `"
							+ getGameVersions(proj) + "`" + "\n **Website Link**: " + "[CurseForge](" + getUrl(proj) + ")",
					false)).
			withField(new FieldEmbed(ZERO_WIDTH_SPACE,
					"**Changelog:** \n```" + getSyntax() + "\n" + formatChangelog(file.changelogPlainText(1000)) + "\n```",
					false)).build();
		webhook.sendMessage(embed);
	}

	/**
	 * Message with direct link.
	 *
	 * @param proj    the proj
	 * @param file    the file
	 * @throws CurseException the curse exception
	 */
	public static void messageWithDirectLink(CurseProject proj, CurseFile file, DiscordWebhook webhook)
			throws CurseException {
		DiscordEmbed embed = new DiscordEmbed.Builder().
			withTitle(proj.name() + proj.url().toString()).
			withThumbnail(new ThumbnailEmbed(proj.logo().thumbnailURL().toString(), 80, 80)).
			withDescription(getMessageDescription()).
			withField(new FieldEmbed(ZERO_WIDTH_SPACE,
					"**Release Type**: `" + file.releaseType().name() + "`" + "\n **File Name**: `" + file.displayName()
							+ "`" + "\n **Category**: `" + proj.categorySection().name() + "`" + "\n **GameVersion**: `"
							+ getGameVersions(proj) + "`" + "\n **Download Link**: " + "[Download](" + file.downloadURL()
							+ ")", false)).
			withField(new FieldEmbed(ZERO_WIDTH_SPACE,
					"**Changelog:** \n```" + getSyntax() + "\n" + formatChangelog(file.changelogPlainText(1000)) + "\n```",
					false)).build();
		webhook.sendMessage(embed);
	}

	/**
	 * Send pingable update notification.
	 *
	 * @param role    the role
	 * @param proj    the proj
	 * @param webhook webhook to send
	 * @throws CurseException the curse exception
	 */
	public static void sendPingableUpdateNotification(String role, CurseProject proj, DiscordWebhook webhook)
			throws CurseException {
		webhook.sendMessage(new DiscordMessage(String.format("<@%s>", role)));
		sendUpdateNotification(proj, webhook);
	}

	/**
	 * Send update notification.
	 *
	 * @param proj    the proj
	 * @param webhook webhook to send
	 * @throws CurseException the curse exception
	 */
	public static void sendUpdateNotification(CurseProject proj, DiscordWebhook webhook) throws CurseException {
		switch (Main.cfg.updateFileLink) {
			case NO_LINK:
				EmbedMessage.messageWithoutLink(proj, proj.files().first(), webhook);
				break;
			case CURSE:
				EmbedMessage.messageWithCurseLink(proj, proj.files().first(), webhook);
				break;
			case DIRECT:
				EmbedMessage.messageWithDirectLink(proj, proj.files().first(), webhook);
				break;
		}
	}

	enum UpdateFileLinkMode {
		NO_LINK, CURSE, DIRECT
	}

	/**
	 * Returns the custom message description set in bot.conf Description will be
	 * set to default description if over 500 characters
	 *
	 * @return description
	 */
	private static String getMessageDescription() {
		String desc = Main.cfg.messageDescription;
		if (desc.length() > 500) {
			System.out.println(
					"Your messageDescription is over 500 characters, setting to default value **PLEASE CHANGE THIS**");
			return "New File detected For CurseForge Project";
		} else {
			return desc;
		}
	}

	/**
	 * Format changelog.
	 *
	 * @param s the s
	 * @return the string
	 */
	private static String formatChangelog(String s) {
		String string = Processor.process(s).replace("<br>", "\n").replace("&lt;", "<").replace("&gt;",
				">").replaceAll("(?s)<[^>]*>(<[^>]*>)*", "");
		string = string.replaceAll("https.*?\\s", "");
		String out = "";
		int additionalLines = 0;
		for (final String st : string.split("\n")) {
			if ((out + st.trim() + "\n").length() > 950) {
				additionalLines++;
			} else // noinspection StringConcatenationInLoop
				out = out + st.trim() + "\n";
		}
		return out + (additionalLines > 0 ? ("... And " + additionalLines + " more lines") : "");
	}

	/**
	 * Gets the game versions.
	 *
	 * @param proj the proj
	 * @return the game versions
	 * @throws CurseException the curse exception
	 */
	@SuppressWarnings("StringConcatenationInLoop")
	private static String getGameVersions(final CurseProject proj) throws CurseException {
		if (proj.files().first().gameVersionStrings().isEmpty())
			return "UNKNOWN";
		String out = "";
		final Stream<String> stream = proj.files().first().gameVersionStrings().stream().sorted();
		for (Iterator<String> it = stream.iterator(); it.hasNext(); ) {
			final String s = it.next();
			out = out + s + (it.hasNext() ? ", " : "");
		}
		return out;
	}

	/**
	 * returns the discord markdown syntax set in bot.conf this method does not
	 * throw an error if syntax is not supported or if multiple syntax's are
	 * specified.
	 * <p>
	 * Non-supported syntax auto default to plain text in discord
	 *
	 * @return discord code syntax
	 */
	private static String getSyntax() {
		String md = Main.cfg.changlogDiscordFormat;
		if (!(md.equals("Syntax"))) {
			return md + "\n";
		} else {
			return "\n";
		}
	}

	/**
	 * Return the newest file curseforge page url to embed into message.
	 *
	 * @param proj the proj
	 * @return url link to file page
	 * @throws CurseException the curse exception
	 */
	private static String getUrl(final CurseProject proj) throws CurseException {
		String urlPre = proj.url().toString();
		int id = proj.files().first().id();
		return urlPre + "/files/" + id;
	}
}
