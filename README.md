# TicketSystem
Плагин тикетов, жалоб и предложений для Minecraft.

Русифицированный форк [henriquemb/TicketSystem](https://github.com/henriquemb/TicketSystem), портированный на **Paper 26.2**.

| | |
| --- | --- |
| Версия плагина | 2.0.0 |
| Сервер | Paper 26.2 (`api-version: '26.2'`) |
| Java | 25+ |
| База данных | SQLite (по умолчанию) или MySQL/MariaDB |
| Зависимости | нет (HikariCP и драйверы БД сервер скачивает сам через `libraries`) |

## Возможности

- **Тикеты** — игрок задаёт вопрос, персонал отвечает, игрок оценивает ответ (от «Ужасно» до «Отлично»).
- **Мои тикеты** — игрок видит свои тикеты, их статус и ответы (`/ticket my` или в меню).
- **GUI-меню** — очереди тикетов, жалоб и предложений для персонала и «Мои тикеты» для игроков.
- **Статистика персонала** — средняя оценка каждого сотрудника и разбивка по оценкам.
- **Жалобы** на игроков со ссылкой на доказательство и статусами «Ожидает», «На рассмотрении», «Принята», «Отклонена».
- **Предложения** игроков с ответами персонала.
- **Анти-спам** — задержка между обращениями, лимит обращений без ответа и минимальная длина текста.
- **MySQL** — одна база на несколько серверов сети.
- **`/support`** — объявление на весь сервер, что сотрудник готов помочь, с кнопкой «Попросить помощи».
- Ответ на тикет или предложение придёт игроку при следующем входе, если в момент ответа он был не в сети.
- Персонал при входе видит, сколько тикетов, жалоб и предложений ждут ответа.
- Кликабельные кнопки в чате: открыть, ответить, телепортироваться, листать страницы.
- Все сообщения и названия в меню настраиваются в языковом файле.

## Установка

1. Скопируйте `TicketSystem-2.0.0.jar` в папку `plugins` сервера.
2. Запустите сервер — появится папка `plugins/TicketSystem`:
   - `config.yml` — настройки;
   - `language/russian.yml`, `language/english.yml` — сообщения;
   - `database.db` — база данных (при `database.type: sqlite`).
3. Выдайте персоналу права (см. ниже) и при необходимости отредактируйте настройки и сообщения, затем выполните `/ts reload`.

При первом запуске сервер скачивает из Maven Central библиотеки из раздела `libraries` в `plugin.yml` (HikariCP, драйверы MySQL и SQLite) — нужен доступ в интернет.

## Команды

### Тикеты

| Команда | Описание | Право |
| ------ | ------ | ----- |
| `/ticket <вопрос>` | Задать вопрос | `ticketsystem.ticket.use` |
| `/ticket my [страница]` | Мои тикеты | `ticketsystem.ticket.use` |
| `/ticket view <id>` | Подробности тикета (игроку — только своего, с кнопками оценки) | `ticketsystem.ticket.use` |
| `/ticket rate <id> <оценка>` | Оценить ответ: `TERRIBLE`, `BAD`, `REGULAR`, `GOOD`, `GREAT` | `ticketsystem.ticket.use` |
| `/ticket menu` | Открыть меню | — |
| `/ticket help` | Список команд | — |
| `/tickets [страница]` | Ожидающие тикеты | `ticketsystem.ticket.staff` |
| `/tickets [страница] -a` | Все отвеченные тикеты | `ticketsystem.ticket.staff` |
| `/tickets [страница] -p <ник>` | Тикеты, на которые ответил сотрудник | `ticketsystem.ticket.staff` |
| `/ticket response <id> <ответ>` | Ответить на тикет | `ticketsystem.ticket.staff` |
| `/ticket teleport <id>` | Телепорт к автору тикета | `ticketsystem.ticket.staff` |
| `/support` | Объявить, что вы готовы помочь | `ticketsystem.ticket.staff` |
| `/ticket stats [страница]` | Рейтинг персонала по средней оценке | `ticketsystem.ticket.admin` |
| `/ticket stats <ник>` | Подробная статистика сотрудника | `ticketsystem.ticket.admin` |
| `/ticket rate <id> CANCELED` | Аннулировать оценку | `ticketsystem.ticket.admin` |
| `/ticket cancelall <ник>` | Аннулировать все оценки сотрудника | `ticketsystem.ticket.admin` |

### Жалобы

| Команда | Описание | Право |
| ------ | ------ | ----- |
| `/report <ник> [ссылка] [причина]` | Пожаловаться на игрока в сети | `ticketsystem.report.use` |
| `/report help` | Список команд | — |
| `/reports [страница] [-a]` | Нерассмотренные жалобы на игроков в сети (`-a` — все жалобы) | `ticketsystem.report.staff` |
| `/report view <id>` | Подробности жалобы | `ticketsystem.report.staff` |
| `/report status <id> <статус>` | Изменить статус: `WAITING`, `REVIEW`, `ACCEPTED`, `REJECT` | `ticketsystem.report.staff` |
| `/report teleport <id>` | Телепорт к нарушителю | `ticketsystem.report.staff` |

Статусы `ACCEPTED` и `REJECT` закрывают жалобу.

### Предложения

| Команда | Описание | Право |
| ------ | ------ | ----- |
| `/suggestion <текст>` | Отправить предложение | `ticketsystem.suggestion.use` |
| `/suggestion help` | Список команд | — |
| `/suggestions [страница] [-a]` | Предложения без ответа (`-a` — все) | `ticketsystem.suggestion.staff` |
| `/suggestion view <id>` | Подробности предложения | `ticketsystem.suggestion.staff` |
| `/suggestion response <id> <ответ>` | Ответить на предложение | `ticketsystem.suggestion.staff` |

### Меню и администрирование

| Команда | Описание | Право |
| ------ | ------ | ----- |
| `/ticketmenu` | Открыть меню | — |
| `/ticketsystem reload` | Перечитать `config.yml` и языковой файл, переподключиться к базе | `ticketsystem.admin` |

### Алиасы

| Команда | Алиасы |
| --- | --- |
| `/ticket` | `/vopros`, `/help-me` |
| `/ticketmenu` | `/tmenu` |
| `/report` | `/zhaloba` |
| `/reports` | `/zhaloby` |
| `/suggestion` | `/predlozhenie`, `/idea` |
| `/suggestions` | `/predlozheniya`, `/ideas` |
| `/support` | `/pomosh`, `/helping` |
| `/ticketsystem` | `/ts` |

## GUI-меню

Открывается командой `/ticketmenu` (`/tmenu`) или `/ticket menu`. Кнопки главного меню зависят от прав:

| Кнопка | Кто видит | Действие |
| --- | --- | --- |
| Задать вопрос | `ticketsystem.ticket.use` | Закрывает меню и вставляет `/ticket ` в чат |
| Мои тикеты | `ticketsystem.ticket.use` | Свои тикеты со статусом, ответом и оценкой; клик — открыть в чате и оценить |
| Тикеты | `ticketsystem.ticket.staff` | Тикеты без ответа; ЛКМ — открыть и ответить, ПКМ — телепорт к игроку |
| Жалобы | `ticketsystem.report.staff` | Все нерассмотренные жалобы; ЛКМ — открыть и сменить статус, ПКМ — телепорт к нарушителю |
| Предложения | `ticketsystem.suggestion.staff` | Предложения без ответа; клик — открыть и ответить |

Списки постраничные (45 элементов на странице). Названия и описания предметов — раздел `gui` языкового файла, материалы иконок — `gui.icons` в `config.yml`. Меню можно отключить: `gui.enabled: false`.

## Анти-спам

Для каждого типа обращений (`ticket`, `report`, `suggestion`) в `config.yml` задаются:

| Параметр | Значение |
| --- | --- |
| `min-words` | Минимум слов в тексте (для жалоб — в причине; `0` — причина необязательна) |
| `cooldown` | Задержка между обращениями одного игрока, в секундах |
| `max-open` | Сколько обращений без ответа может быть у игрока одновременно |

`0` в `cooldown` и `max-open` отключает ограничение. Право `ticketsystem.bypass.antispam` снимает задержку и лимит (минимальная длина текста действует для всех).

## Права

| Право | Описание | По умолчанию |
| --- | --- | --- |
| `ticketsystem.ticket.use` | Создавать тикеты, смотреть и оценивать свои | все |
| `ticketsystem.report.use` | Отправлять жалобы | все |
| `ticketsystem.suggestion.use` | Отправлять предложения | все |
| `ticketsystem.ticket.staff` | Работа с тикетами, `/support`, уведомления о новых тикетах | OP |
| `ticketsystem.report.staff` | Работа с жалобами, уведомления о новых жалобах | OP |
| `ticketsystem.suggestion.staff` | Работа с предложениями, уведомления о новых предложениях | OP |
| `ticketsystem.ticket.admin` | Статистика персонала, аннулирование оценок | OP |
| `ticketsystem.admin` | `/ts reload` | OP |
| `ticketsystem.bypass.antispam` | Без задержек и лимитов обращений | OP |

## Настройка

Все параметры `config.yml` с комментариями:

```yaml
language: russian            # файл из папки language (без .yml)

database:
  type: sqlite               # sqlite или mysql
  mysql:
    host: localhost
    port: 3306
    database: ticketsystem
    username: root
    password: ""
    parameters: "useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8"
    pool-size: 5

ticket:      { min-words: 4, cooldown: 60,  max-open: 3 }
report:      { min-words: 0, cooldown: 60,  max-open: 5 }
suggestion:  { min-words: 4, cooldown: 120, max-open: 3 }

support-cooldown: 10         # минуты между /support

pagination:
  page-size: 10              # строк на странице списков в чате

notifications:               # звук уведомлений ("" — без звука)
  sound: entity.experience_orb.pickup
  volume: 1.0
  pitch: 1.0

gui:
  enabled: true
  icons: { ... }             # материалы иконок меню
```

После обновления плагина новые параметры автоматически добавляются в существующий `config.yml`, а отсутствующие ключи языкового файла берутся из встроенного.

### MySQL

1. Создайте базу: `CREATE DATABASE ticketsystem CHARACTER SET utf8mb4;`
2. Укажите `database.type: mysql` и данные подключения.
3. Выполните `/ts reload` или перезапустите сервер — таблицы создадутся автоматически.

Если несколько серверов подключены к одной базе, тикеты, жалобы и предложения общие. Телепорт работает только к игрокам на том же сервере. Данные из SQLite в MySQL автоматически не переносятся.

### Свой язык

Скопируйте `language/russian.yml` под новым именем, переведите и укажите его в `language`. Если файл не найден, используется `russian.yml`.

### Формат сообщений

| Запись | Результат |
| --- | --- |
| `&a`, `&l`, `&r` … | Цвета и форматирование |
| `&#ff8800` | HEX-цвет |
| `[текст](/команда hover=подсказка)` | Кнопка, выполняющая команду |
| `[текст](suggest_command=/команда hover=подсказка)` | Кнопка, вставляющая команду в чат |
| `[текст](https://сайт hover=подсказка)` | Кнопка-ссылка |
| `\&`, `\[` | Символ без специального значения |

Плейсхолдеры (`<player>`, `<id>`, `<ticket>`, `<button-view>` и т.д.) приведены в `russian.yml` в тех сообщениях, где они доступны. В описаниях предметов меню `<text>`, `<response>` и `<evidence>` автоматически переносятся на несколько строк.

## Отличия от оригинала

- Перенос со Spigot 1.16.5 на Paper API 26.2 и Java 25.
- Сообщения выводятся через Adventure вместо устаревшего BungeeCord chat API; библиотека MineDown заменена встроенным парсером, формат языковых файлов сохранён.
- Полный перевод на русский: сообщения, логи, описания команд и прав. Язык по умолчанию — `russian`, португальский удалён.
- Новое: GUI-меню, `/ticket my`, анти-спам, поддержка MySQL, настройки звука, длины текста и размера страниц в `config.yml`.
- `/ts reload` перезагружает конфиг без выключения плагина и работает из консоли.
- Убрана проверка обновлений на SpigotMC.
- Исправлено:
  - не работала задержка `/support`;
  - неверная оценка или статус в `/ticket rate` и `/report status` вызывали ошибку;
  - `/report <ник>` без причины вызывал ошибку, подсказки ников по Tab не работали;
  - при входе игроку приходили его же предложения без ответа как «ответы», после чего настоящий ответ уже не доставлялся;
  - соединения с базой данных не закрывались полностью.

## Сборка

```sh
mvn clean package
```

Готовый файл: `target/TicketSystem-2.0.0.jar`.

## Лицензия

GNU GPL v3, как у оригинального проекта. Текст лицензии: [LICENSE](LICENSE).
