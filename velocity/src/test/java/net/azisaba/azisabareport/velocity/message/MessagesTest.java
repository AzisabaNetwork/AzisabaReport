package net.azisaba.azisabareport.velocity.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {
    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    void allTranslationsUseMiniMessagePlaceholders() throws Exception {
        for (String language : new String[]{"en", "ja"}) {
            try (InputStream in = Messages.class.getResourceAsStream("/messages_" + language + ".yml")) {
                assertNotNull(in);
                Map<String, String> translations = new Yaml().load(in);
                assertEquals(23, translations.size());
                translations.forEach((key, template) -> {
                    assertFalse(template.matches(".*&[0-9a-fk-or].*"), key);
                    assertFalse(template.contains("%s"), key);
                    String rendered = plain(Messages.format(template, "Alice", "Reason"));
                    String expected = template.replace("<arg0>", "Alice").replace("<arg1>", "Reason");
                    expected = MiniMessage.miniMessage().stripTags(expected);
                    assertEquals(expected, rendered, language + ": " + key);
                });
            }
        }
    }

    @Test
    void userInputCannotInjectTagsOrClickEvents() {
        String input = "<click:run_command:'/op Alice'><red>100% &a</red></click>";
        Component component = Messages.format("<green><arg0>", input);
        assertEquals(input, plain(component));
        assertEquals(NamedTextColor.GREEN, component.color());
        assertNoClicks(component);
    }

    @Test
    void uploaderLinkIsClickableAndStyleDoesNotLeak() throws Exception {
        String url = "https://example.com/upload/123?q=a'b&next=%3Ctest%3E";
        ClickEvent click = ClickEvent.openUrl(url);
        for (String language : new String[]{"en", "ja"}) {
            MessageInstance messages = Messages.load(language);
            assertNotNull(messages);
            Component rendered = Messages.format(messages.get("command.report.uploader"),
                    Component.text(url).clickEvent(click));
            assertTrue(plain(rendered).contains(url));
            assertLinkStyles(rendered, null, TextDecoration.State.FALSE, null, url, click);
        }
    }

    @Test
    void localeFallbackAndMultipleArgumentsWork() throws Exception {
        Messages.load();
        String template = Messages.getInstance(Locale.forLanguageTag("zz")).get("command.report.reported");
        assertEquals("Reported Alice for Spam. Thank you for your report!",
                plain(Messages.format(template, "Alice", "Spam")));
        assertEquals(Messages.getInstance(Locale.ENGLISH).get("generic.send"),
                Messages.getInstance(null).get("generic.send"));
    }

    @Test
    void newlinesAndLiteralPercentSignsArePreserved() throws Exception {
        MessageInstance messages = Messages.load("en");
        assertNotNull(messages);
        assertTrue(plain(Messages.format(messages.get("command.azisaba_report.reload.success"))).contains("\n"));
        assertEquals("100% complete", plain(Messages.format("<green>100% complete")));
    }

    private static void assertNoClicks(Component component) {
        assertNull(component.clickEvent());
        component.children().forEach(MessagesTest::assertNoClicks);
    }

    private static void assertLinkStyles(Component component, net.kyori.adventure.text.format.TextColor color,
                                         TextDecoration.State underline, ClickEvent event,
                                         String url, ClickEvent expectedClick) {
        if (component.color() != null) color = component.color();
        if (component.decoration(TextDecoration.UNDERLINED) != TextDecoration.State.NOT_SET) {
            underline = component.decoration(TextDecoration.UNDERLINED);
        }
        if (component.clickEvent() != null) event = component.clickEvent();
        if (component instanceof TextComponent text && !text.content().isEmpty()) {
            if (text.content().equals(url)) {
                assertEquals(expectedClick, event);
                assertEquals(NamedTextColor.AQUA, color);
                assertEquals(TextDecoration.State.TRUE, underline);
            } else {
                assertNull(event);
                assertEquals(NamedTextColor.YELLOW, color);
                assertNotEquals(TextDecoration.State.TRUE, underline);
            }
        }
        for (Component child : component.children()) {
            assertLinkStyles(child, color, underline, event, url, expectedClick);
        }
    }
}
