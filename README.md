# TicketSystem
Плагин тикетов, жалоб и предложений для Minecraft (Paper 26.2, Java 25+).

## Возможности

- Тикеты (вопросы игроков) с ответами персонала и оценкой ответа.
- Жалобы на игроков и предложения.
- Данные хранятся в SQLite (`plugins/TicketSystem/database.db`).
- Все сообщения настраиваются в `plugins/TicketSystem/language/russian.yml`.

## Модули

### Тикеты

| Команда | Описание | Право |
| ------ | ------ | ----- |
| ticket help | Помощь |
| support \| pomosh | Объявить, что вы готовы помочь | ticketsystem.ticket.staff
| ticket \<вопрос\> \| vopros | Задать вопрос | ticketsystem.ticket.use
| tickets | Список ожидающих тикетов (`-a` — отвеченные, `-p <ник>` — ответы сотрудника) | ticketsystem.ticket.staff
| ticket rate | Оценить ответ | ticketsystem.ticket.use
| ticket response | Ответить на тикет | ticketsystem.ticket.staff
| ticket stats | Статистика персонала | ticketsystem.ticket.admin
| ticket cancelall \<ник\> | Отменить все оценки сотрудника | ticketsystem.ticket.admin
| ticket teleport | Телепорт к автору тикета | ticketsystem.ticket.staff
| ticket view | Подробности тикета | ticketsystem.ticket.staff

### Жалобы

| Команда | Описание | Право |
| ------ | ------ | ----- |
| report help | Помощь |
| report \<ник\> [ссылка] [причина] \| zhaloba | Пожаловаться на игрока | ticketsystem.report.use
| reports | Список жалоб | ticketsystem.report.staff
| report status | Изменить статус жалобы | ticketsystem.report.staff
| report teleport | Телепорт к нарушителю | ticketsystem.report.staff
| report view | Подробности жалобы | ticketsystem.report.staff

### Предложения

| Команда | Описание | Право |
| ------ | ------ | ----- |
| suggestion help | Помощь |
| suggestion \<текст\> \| idea | Отправить предложение | ticketsystem.suggestion.use
| suggestions | Список предложений | ticketsystem.suggestion.staff
| suggestion response | Ответить на предложение | ticketsystem.suggestion.staff
| suggestion view | Подробности предложения | ticketsystem.suggestion.staff

### Администрирование
Перезагрузка конфигурации и языкового файла:
```sh
ticketsystem reload  # или ts reload
```
Право: `ticketsystem.admin`

## Сборка
```sh
mvn clean package
```
Готовый файл: `target/TicketSystem-2.0.0.jar`.
