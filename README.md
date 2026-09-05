# AzisabaReport

## Build and runtime requirements

- Build with JDK 25 using `./gradlew build` (Windows: `gradlew.bat build`).
- The Velocity plugin requires Velocity 4.1.1 or later and Java 25. It uses the
  proxy-provided Adventure / MiniMessage 5.2.0 API; Adventure is not bundled.
- The common and Spigot modules continue to target Java 8. Gradle provisions the
  Java 8 toolchain when necessary.

## Velocity messages

`velocity/src/main/resources/messages_en.yml` and `messages_ja.yml` use
[MiniMessage](https://docs.papermc.io/adventure/minimessage/format/).
Use tags such as `<green>` and `<underlined>` instead of legacy `&` color codes.
Positional arguments are `<arg0>`, `<arg1>`, etc., instead of `%s`.
Text arguments are inserted literally so report reasons and staff comments cannot
inject MiniMessage tags.

For `command.report.uploader`, `<arg0>` is a clickable URL component generated
from the configured uploader URL after replacing `{id}` with the report ID.
Keep this placeholder in the translation; its surrounding tags control the link
color and underline. Only the URL opens the uploader, not the surrounding text.
