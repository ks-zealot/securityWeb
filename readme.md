# Цель

Приложение представляет собой обертку вокруг сервера Gryzzly, написано чтобы изучить механизмы аутентификации и авторизации

# Структура приложения

Приложение представляет собой http сервер, сконифгурированный в рантайме, с минмумом конфигурационных файлов
Можно конечно использовать Spring MVC + Spring Security, но данное решение потащило бы за собой тонну зависимостей
Движок аутентификации это Apache Shiro, рекомендованный сообществом OWASP
В приложении реализовано OAuth 2.0
Механизм OAuth 2.0 следующий
- Клиент обращается к ссылке на удаленный https сервис, передавая request token
- Ему предлагают залогиниться, потом перенаправляют на страницу http сервиса,на которой отображена ссылка
 обратно на https сервис с access token
- При переходе по этой ссылке access token сверяется с выданным, потом в случае успеха возвращается статус success
- Для хранения выданных\сгенерированных токенов используются заглушки в виде ConcurrentMap и ConcurrentLinkedQueue. По
 хорошему надо использовать базу данных, например Cassandra, которая из коробки умеет  настраваить ttl записи
Для запуска надо выполнить команду
```sh
git clone https://github.com/ks-zealot/securityWeb.git
cd securityWeb/
```

```sh
mvn compile exec:java -Dexec.mainClass="com.sec.web.app.Backend"  -Dexec.args="-n 'OAuth client web application'"
```
(клиентская часть OAUTH инфраструктуры)
и
```sh
mvn compile exec:java -Dexec.mainClass="com.sec.web.app.Backend"  -Dexec.args="-n 'OAuth server web application'"
```
серверная часть инфрастуктуры
Для реализации протокола SSL используется сертификат server.cert в папке ресурсов проекта, который уже добавлен в
 keystore_server
Они находятся в корне проекта
Настройки shiro находятся в *shiro.ini*
Там описаны три аккаунта *admin1, admin2, admin3*
C паролями *password1, password2, password3*
Админ1 имеет все права, админ2 имеет право на первый защищенный ресурс, админ3  на защищенный второй
Для редактирование параметров приложения (клиентский айдишник, реквест токен, адреса на ктоорых будут подняты сервера)
нужно обратиться в ./resources/security.properties
# Устройство http сервера
Сервер запускается в классе GryzzlyWrapper и представляет собой обертку вокруг сервера Gryzzly. На нем регистрируются
 два handlera по адресам */login*  и */secRes*
 Они обрабатываею запросы по разному в зависимости от заданного имени приложения (или клиент или сервер)
 Сначала надо перейти по по URI */login* клиентской части, пройти там авторизацию, перейти к странице /secres
 На странице secRes можно обратиться к двум защищенным ресурсам и одному защищенному ресурсу расположенному на другом хосте
 При обращении к удаленному ресурсу произойдет перенаправление на форму логина удаленного ресурса, потом перенаправление
  обратно на клиентскую страницу с предложением перейти по ссылке.
  При переходе по ссылке предполагается что активирован некий защищеный ресурс, после чего перенаправляет на страницу с
   информацией об успешном выполнении активации
# Зависимости
 * log4j - логирование
 * slf4j-nop -внутренне логирование shiro
 * shiro - секурность, аутентификация
 * gryzzly-http-server - хттп сервер, обработка запросов
 * commons-cli, commons-configuration - прикладные зависимости, парсинг параметров командной строки, считывание файла настроек
 * guava - набор библиотек гугла, заргрузка файлов с жесткого диска
