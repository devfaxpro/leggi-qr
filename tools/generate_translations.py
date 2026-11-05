# SPDX-License-Identifier: GPL-3.0-only
# Copyright (C) 2026 devfaxpro
from pathlib import Path
from xml.sax.saxutils import escape

# Columns: Italian, English, Spanish, Brazilian Portuguese, French, German,
# Simplified Chinese, Japanese, Korean, Russian. Native language labels live in code.
DATA = r'''
tagline|Scansiona. Crea. Conserva.|Scan. Create. Keep.|Escanea. Crea. Guarda.|Escaneie. Crie. Guarde.|Scannez. Créez. Gardez.|Scannen. Erstellen. Speichern.|扫描 · 创建 · 收藏|スキャン・作成・保存|스캔 · 생성 · 보관|Сканируйте. Создавайте. Храните.
scan|Scansiona|Scan|Escanear|Escanear|Scanner|Scannen|扫描|スキャン|스캔|Сканер
create|Crea|Create|Crear|Criar|Créer|Erstellen|创建|作成|만들기|Создать
collection|Raccolta|Collection|Colección|Coleção|Collection|Sammlung|收藏库|コレクション|보관함|Коллекция
settings|Impostazioni|Settings|Ajustes|Configurações|Réglages|Einstellungen|设置|設定|설정|Настройки
language|Lingua|Language|Idioma|Idioma|Langue|Sprache|语言|言語|언어|Язык
system_language|Segui il sistema|Follow system|Usar idioma del sistema|Usar idioma do sistema|Langue du système|Systemsprache|跟随系统|システムの言語|시스템 언어 사용|Язык системы
back|Indietro|Back|Atrás|Voltar|Retour|Zurück|返回|戻る|뒤로|Назад
close|Chiudi|Close|Cerrar|Fechar|Fermer|Schließen|关闭|閉じる|닫기|Закрыть
cancel|Annulla|Cancel|Cancelar|Cancelar|Annuler|Abbrechen|取消|キャンセル|취소|Отмена
save|Salva|Save|Guardar|Salvar|Enregistrer|Speichern|保存|保存|저장|Сохранить
done|Termina scansione|Finish scanning|Terminar escaneo|Concluir leitura|Terminer le scan|Scannen beenden|完成扫描|スキャンを終了|스캔 완료|Завершить сканирование
offline_badge|SEMPLICE. PRIVATO. OFFLINE.|SIMPLE. PRIVATE. OFFLINE.|SENCILLO. PRIVADO. SIN CONEXIÓN.|SIMPLES. PRIVADO. OFFLINE.|SIMPLE. PRIVÉ. HORS LIGNE.|EINFACH. PRIVAT. OFFLINE.|简单 · 私密 · 离线|シンプル・安心・オフライン|간편하게 · 안전하게 · 오프라인|ПРОСТО. ПРИВАТНО. ОФЛАЙН.
hero_title|Un piccolo codice.\nTante possibilità.|A little code.\nA world of possibility.|Un pequeño código.\nMuchas posibilidades.|Um pequeno código.\nMuitas possibilidades.|Un petit code.\nTant de possibilités.|Ein kleiner Code.\nViele Möglichkeiten.|小小二维码，\n开启更多可能。|小さなコードに、\n広がる可能性。|작은 코드 하나,\n무한한 가능성.|Маленький код.\nМного возможностей.
hero_subtitle|Link, idee e connessioni. Tutto a portata di scansione.|Links, ideas and connections. Just a scan away.|Enlaces, ideas y conexiones. A un escaneo de distancia.|Links, ideias e conexões. A uma leitura de distância.|Liens, idées et connexions. À portée de scan.|Links, Ideen und Verbindungen. Nur einen Scan entfernt.|链接、灵感与连接，扫一扫即可触达。|リンクもアイデアも、スキャンひとつで。|링크, 아이디어, 연결을 스캔 한 번으로.|Ссылки, идеи и контакты — одним сканированием.
scan_now|Scansiona un codice|Scan a code|Escanear un código|Escanear um código|Scanner un code|Code scannen|扫描二维码或条形码|コードをスキャン|코드 스캔하기|Сканировать код
import_image|Importa da un’immagine|Import from an image|Importar desde una imagen|Importar de uma imagem|Importer une image|Aus Bild importieren|从图片导入|画像から読み込む|이미지에서 가져오기|Импорт из изображения
quick_create|Crea al volo|Quick create|Creación rápida|Criação rápida|Création rapide|Schnell erstellen|快速创建|すぐに作成|빠르게 만들기|Быстрое создание
view_all|Vedi tutti|View all|Ver todos|Ver todos|Tout voir|Alle anzeigen|查看全部|すべて見る|모두 보기|Все
recent|I tuoi ultimi codici|Your recent codes|Tus últimos códigos|Seus últimos códigos|Vos derniers codes|Deine letzten Codes|最近的代码|最近のコード|최근 코드|Последние коды
empty_recent|Il prossimo codice ti aspetta|Your next code awaits|Tu próximo código te espera|Seu próximo código espera por você|Votre prochain code vous attend|Dein nächster Code wartet|等待你的下一个代码|次のコードを読み取ろう|새 코드를 만나 보세요|Вас ждёт новый код
empty_recent_hint|Scansiona o crea un codice: lo ritroverai qui.|Scan or create a code to find it here.|Escanea o crea un código y aparecerá aquí.|Escaneie ou crie um código para vê-lo aqui.|Scannez ou créez un code pour le retrouver ici.|Scanne oder erstelle einen Code, um ihn hier zu finden.|扫描或创建代码后，就能在这里找到。|スキャンまたは作成したコードがここに表示されます。|코드를 스캔하거나 만들면 여기에 표시됩니다.|Сканируйте или создайте код, и он появится здесь.
create_title|Dai forma alle idee.|Give your ideas a code.|Dale un código a tus ideas.|Dê um código às suas ideias.|Donnez un code à vos idées.|Gib deinen Ideen einen Code.|让想法变成二维码。|アイデアをコードに。|아이디어를 코드로.|Превратите идеи в код.
create_hint|Scegli cosa vuoi condividere.|Choose what you want to share.|Elige qué quieres compartir.|Escolha o que deseja compartilhar.|Choisissez ce que vous voulez partager.|Wähle, was du teilen möchtest.|选择你想分享的内容。|共有したい内容を選んでください。|공유할 내용을 선택하세요.|Выберите, чем хотите поделиться.
url|Link|Link|Enlace|Link|Lien|Link|链接|リンク|링크|Ссылка
text|Testo|Text|Texto|Texto|Texte|Text|文本|テキスト|텍스트|Текст
wifi|Wi-Fi|Wi-Fi|Wi-Fi|Wi-Fi|Wi-Fi|WLAN|Wi-Fi|Wi-Fi|Wi-Fi|Wi-Fi
contact|Contatto|Contact|Contacto|Contato|Contact|Kontakt|联系人|連絡先|연락처|Контакт
email|Email|Email|Correo|E-mail|E-mail|E-Mail|电子邮件|メール|이메일|Почта
sms|Messaggio|Message|Mensaje|Mensagem|Message|Nachricht|短信|メッセージ|메시지|Сообщение
phone|Telefono|Phone|Teléfono|Telefone|Téléphone|Telefon|电话|電話|전화|Телефон
location|Posizione|Location|Ubicación|Localização|Position|Standort|位置|位置情報|위치|Место
event|Evento|Event|Evento|Evento|Événement|Termin|日程|予定|일정|Событие
barcode|Codice a barre|Barcode|Código de barras|Código de barras|Code-barres|Strichcode|条形码|バーコード|바코드|Штрихкод
url_hint|Un sito, una pagina, un profilo|A website, page or profile|Un sitio, una página o un perfil|Um site, uma página ou um perfil|Un site, une page, un profil|Website, Seite oder Profil|网站、页面或个人主页|ウェブサイトやプロフィール|웹사이트, 페이지, 프로필|Сайт, страница или профиль
text_hint|Parole, note e piccoli pensieri|Words, notes and little ideas|Palabras, notas e ideas|Palavras, notas e ideias|Mots, notes et petites idées|Worte, Notizen und Ideen|文字、笔记与灵感|言葉やメモ、アイデア|글, 메모, 작은 아이디어|Текст, заметки и идеи
wifi_hint|Condividi l’accesso alla rete|Share access to a network|Comparte el acceso a la red|Compartilhe o acesso à rede|Partagez l’accès au réseau|Netzwerkzugang teilen|分享网络连接信息|ネットワーク情報を共有|네트워크 연결 정보 공유|Доступ к сети
contact_hint|Un biglietto da visita digitale|A digital business card|Una tarjeta de visita digital|Um cartão de visita digital|Une carte de visite numérique|Eine digitale Visitenkarte|电子名片|デジタル名刺|디지털 명함|Цифровая визитка
email_hint|Indirizzo, oggetto e messaggio|Address, subject and message|Dirección, asunto y mensaje|Endereço, assunto e mensagem|Adresse, objet et message|Adresse, Betreff und Nachricht|地址、主题与正文|宛先・件名・本文|주소, 제목, 본문|Адрес, тема и текст
sms_hint|Un SMS pronto da scrivere|Prepare a text message|Prepara un SMS|Prepare uma mensagem SMS|Préparez un SMS|Eine SMS vorbereiten|准备短信内容|SMSを準備|문자 메시지 준비|Подготовьте SMS
phone_hint|Un numero da chiamare|A number to call|Un número para llamar|Um número para ligar|Un numéro à appeler|Eine Nummer zum Anrufen|要拨打的号码|電話番号|전화할 번호|Номер телефона
location_hint|Un punto preciso sulla mappa|A precise point on the map|Un punto exacto en el mapa|Um ponto exato no mapa|Un point précis sur la carte|Ein genauer Punkt auf der Karte|地图上的精确位置|地図上の正確な位置|지도 위의 정확한 위치|Точка на карте
event_hint|Un appuntamento in calendario|An event for the calendar|Una cita en el calendario|Um compromisso no calendário|Un rendez-vous dans l’agenda|Ein Termin im Kalender|添加到日历的活动|カレンダーの予定|캘린더에 추가할 일정|Событие в календаре
barcode_hint|EAN, Code 128, Aztec e altri|EAN, Code 128, Aztec and more|EAN, Code 128, Aztec y más|EAN, Code 128, Aztec e outros|EAN, Code 128, Aztec et plus|EAN, Code 128, Aztec und mehr|EAN、Code 128、Aztec 等|EAN・Code 128・Aztec など|EAN, Code 128, Aztec 등|EAN, Code 128, Aztec и другие
form_hint|Inserisci i dettagli. Il codice viene creato sul telefono.|Enter the details. Your code is created on your phone.|Introduce los datos. El código se crea en tu teléfono.|Insira os dados. O código é criado no seu celular.|Saisissez les détails. Le code est créé sur votre téléphone.|Gib die Details ein. Der Code wird auf deinem Gerät erstellt.|填写信息，代码将在手机上生成。|情報を入力してください。コードは端末内で作成されます。|정보를 입력하세요. 코드는 휴대전화에서 생성됩니다.|Введите данные. Код создаётся на телефоне.
url_field|Indirizzo web|Web address|Dirección web|Endereço web|Adresse web|Webadresse|网址|ウェブアドレス|웹 주소|Веб-адрес
network|Nome della rete|Network name|Nombre de la red|Nome da rede|Nom du réseau|Netzwerkname|网络名称|ネットワーク名|네트워크 이름|Название сети
password|Password|Password|Contraseña|Senha|Mot de passe|Passwort|密码|パスワード|비밀번호|Пароль
name|Nome completo|Full name|Nombre completo|Nome completo|Nom complet|Vollständiger Name|姓名|氏名|이름|Полное имя
phone_field|Numero di telefono|Phone number|Número de teléfono|Número de telefone|Numéro de téléphone|Telefonnummer|电话号码|電話番号|전화번호|Номер телефона
email_field|Indirizzo email|Email address|Correo electrónico|Endereço de e-mail|Adresse e-mail|E-Mail-Adresse|电子邮箱|メールアドレス|이메일 주소|Адрес почты
company|Azienda|Company|Empresa|Empresa|Entreprise|Firma|公司|会社名|회사|Компания
website|Sito web|Website|Sitio web|Site|Site web|Website|网站|ウェブサイト|웹사이트|Сайт
subject|Oggetto|Subject|Asunto|Assunto|Objet|Betreff|主题|件名|제목|Тема
message|Messaggio|Message|Mensaje|Mensagem|Message|Nachricht|内容|本文|메시지|Сообщение
latitude|Latitudine|Latitude|Latitud|Latitude|Latitude|Breitengrad|纬度|緯度|위도|Широта
longitude|Longitudine|Longitude|Longitud|Longitude|Longitude|Längengrad|经度|経度|경도|Долгота
event_title|Titolo dell’evento|Event title|Título del evento|Título do evento|Titre de l’événement|Termintitel|日程标题|予定のタイトル|일정 제목|Название события
start|Inizio|Start|Inicio|Início|Début|Beginn|开始|開始|시작|Начало
end|Fine|End|Fin|Fim|Fin|Ende|结束|終了|종료|Окончание
place|Luogo|Place|Lugar|Local|Lieu|Ort|地点|場所|장소|Место
content|Contenuto|Content|Contenido|Conteúdo|Contenu|Inhalt|内容|内容|내용|Содержимое
security|Sicurezza della rete|Network security|Seguridad de la red|Segurança da rede|Sécurité du réseau|Netzwerksicherheit|网络安全类型|ネットワークの暗号化|네트워크 보안|Защита сети
open_network|Rete aperta|Open network|Red abierta|Rede aberta|Réseau ouvert|Offenes Netzwerk|开放网络|暗号化なし|개방형 네트워크|Открытая сеть
hidden_network|Rete nascosta|Hidden network|Red oculta|Rede oculta|Réseau masqué|Verstecktes Netzwerk|隐藏网络|非公開ネットワーク|숨겨진 네트워크|Скрытая сеть
format|Formato|Format|Formato|Formato|Format|Format|格式|形式|형식|Формат
date_hint|Formato: 2026-12-31 18:30 (ora locale)|Format: 2026-12-31 18:30 (local time)|Formato: 2026-12-31 18:30 (hora local)|Formato: 2026-12-31 18:30 (hora local)|Format : 2026-12-31 18:30 (heure locale)|Format: 2026-12-31 18:30 (Ortszeit)|格式：2026-12-31 18:30（当地时间）|形式：2026-12-31 18:30（現地時間）|형식: 2026-12-31 18:30 (현지 시간)|Формат: 2026-12-31 18:30 (местное время)
barcode_help|EAN-13: 12 o 13 cifre. EAN-8: 7 o 8 cifre. ITF: numero pari di cifre. Code 39: lettere maiuscole e cifre.|EAN-13: 12 or 13 digits. EAN-8: 7 or 8 digits. ITF: an even number of digits. Code 39: uppercase letters and digits.|EAN-13: 12 o 13 dígitos. EAN-8: 7 u 8. ITF: cantidad par de dígitos. Code 39: mayúsculas y dígitos.|EAN-13: 12 ou 13 dígitos. EAN-8: 7 ou 8. ITF: quantidade par de dígitos. Code 39: letras maiúsculas e dígitos.|EAN-13 : 12 ou 13 chiffres. EAN-8 : 7 ou 8. ITF : nombre pair de chiffres. Code 39 : majuscules et chiffres.|EAN-13: 12 oder 13 Ziffern. EAN-8: 7 oder 8. ITF: gerade Ziffernanzahl. Code 39: Großbuchstaben und Ziffern.|EAN-13：12或13位数字。EAN-8：7或8位数字。ITF：偶数位数字。Code 39：大写字母和数字。|EAN-13：12または13桁。EAN-8：7または8桁。ITF：偶数桁の数字。Code 39：英大文字と数字。|EAN-13: 숫자 12~13자리. EAN-8: 7~8자리. ITF: 짝수 자릿수. Code 39: 영문 대문자와 숫자.|EAN-13: 12 или 13 цифр. EAN-8: 7 или 8. ITF: чётное число цифр. Code 39: заглавные латинские буквы и цифры.
generate|Crea il codice|Create code|Crear código|Criar código|Créer le code|Code erstellen|生成代码|コードを作成|코드 만들기|Создать код
private_hint|Nessun caricamento online. Nessun account richiesto.|No uploads. No account needed.|Sin subidas a internet. Sin cuenta.|Sem envio para a internet. Sem cadastro.|Aucun envoi en ligne. Aucun compte requis.|Kein Hochladen. Kein Konto erforderlich.|无需上传，无需注册。|アップロード不要。アカウント不要。|업로드도 회원가입도 필요 없어요.|Без загрузки в интернет и без аккаунта.
invalid_input|Controlla i campi e il formato scelto. Se il testo è molto lungo, accorcialo.|Check the fields and selected format. Shorten very long text.|Revisa los campos y el formato. Acorta los textos muy largos.|Confira os campos e o formato. Reduza textos muito longos.|Vérifiez les champs et le format. Raccourcissez les textes trop longs.|Prüfe die Felder und das Format. Kürze sehr lange Texte.|请检查输入和所选格式。内容过长时请缩短。|入力内容と形式を確認してください。長い文章は短くしてください。|입력 항목과 형식을 확인하세요. 너무 긴 내용은 줄여 주세요.|Проверьте поля и формат. Сократите слишком длинный текст.
collection_title|Tutto, al suo posto.|Everything in its place.|Todo en su sitio.|Tudo no seu lugar.|Chaque code à sa place.|Alles an seinem Platz.|井然有序，随时可用。|すべて、ここに。|모든 코드를 한곳에.|Всё на своём месте.
collection_hint|I codici che vuoi ritrovare, sempre con te.|The codes you want to keep, always with you.|Los códigos que quieres guardar, siempre contigo.|Os códigos que você quer guardar, sempre com você.|Vos codes à conserver, toujours avec vous.|Deine gespeicherten Codes, immer dabei.|想保留的代码，随时随地都能找到。|大切なコードを、いつでも手元に。|보관한 코드를 언제든 찾아보세요.|Нужные коды всегда под рукой.
search|Cerca contenuto o nome|Search content or name|Buscar contenido o nombre|Buscar conteúdo ou nome|Rechercher un contenu ou un nom|Inhalt oder Name suchen|搜索内容或名称|内容や名前で検索|내용 또는 이름 검색|Поиск по содержимому или имени
all|Tutti i codici|All codes|Todos los códigos|Todos os códigos|Tous les codes|Alle Codes|全部代码|すべてのコード|모든 코드|Все коды
favorites|Preferiti|Favorites|Favoritos|Favoritos|Favoris|Favoriten|收藏|お気に入り|즐겨찾기|Избранное
created|Creati|Created|Creados|Criados|Créés|Erstellt|已创建|作成したコード|생성한 코드|Созданные
scanned|Scansionati|Scanned|Escaneados|Escaneados|Scannés|Gescannt|已扫描|スキャンしたコード|스캔한 코드|Отсканированные
empty_collection|Nessun codice trovato|No codes found|No se encontraron códigos|Nenhum código encontrado|Aucun code trouvé|Keine Codes gefunden|未找到代码|コードが見つかりません|코드를 찾을 수 없어요|Коды не найдены
empty_collection_hint|Crea o scansiona un codice, oppure cambia la ricerca.|Create or scan a code, or try another search.|Crea o escanea un código, o cambia la búsqueda.|Crie ou escaneie um código, ou altere a busca.|Créez ou scannez un code, ou modifiez la recherche.|Erstelle oder scanne einen Code oder ändere die Suche.|创建或扫描代码，或尝试其他搜索词。|コードを作成・スキャンするか、検索条件を変えてください。|코드를 만들거나 스캔해 보세요. 검색어를 바꿔도 좋아요.|Создайте или отсканируйте код либо измените запрос.
your_code|Il tuo codice|Your code|Tu código|Seu código|Votre code|Dein Code|你的代码|作成したコード|내 코드|Ваш код
result|Codice trovato|Code found|Código encontrado|Código encontrado|Code trouvé|Code gefunden|识别结果|読み取り結果|스캔 결과|Код найден
code_preview|Anteprima del codice|Code preview|Vista previa del código|Prévia do código|Aperçu du code|Code-Vorschau|代码预览|コードのプレビュー|코드 미리보기|Предпросмотр кода
preview_unavailable|Anteprima non disponibile per questo contenuto.|Preview unavailable for this content.|Vista previa no disponible para este contenido.|Prévia indisponível para este conteúdo.|Aperçu indisponible pour ce contenu.|Vorschau für diesen Inhalt nicht verfügbar.|此内容无法预览。|この内容のプレビューは表示できません。|이 내용은 미리 볼 수 없어요.|Предпросмотр этого содержимого недоступен.
show_content|Mostra il contenuto completo|Show full content|Mostrar contenido completo|Mostrar conteúdo completo|Afficher tout le contenu|Vollständigen Inhalt anzeigen|显示完整内容|すべての内容を表示|전체 내용 보기|Показать полностью
copy|Copia|Copy|Copiar|Copiar|Copier|Kopieren|复制|コピー|복사|Копировать
favorite|Preferito|Favorite|Favorito|Favorito|Favori|Merken|收藏|お気に入り|즐겨찾기|В избранное
favorited|Nei preferiti|Favorited|En favoritos|Nos favoritos|Dans les favoris|Gemerkt|已收藏|登録済み|즐겨찾기됨|В избранном
color|Un tocco di colore|A touch of color|Un toque de color|Um toque de cor|Une touche de couleur|Ein Hauch Farbe|添一点色彩|彩りを添えて|색상 선택|Немного цвета
sage|Salvia|Sage|Salvia|Sálvia|Sauge|Salbei|鼠尾草绿|セージ|세이지|Шалфей
lavender|Lavanda|Lavender|Lavanda|Lavanda|Lavande|Lavendel|薰衣草紫|ラベンダー|라벤더|Лаванда
cocoa|Cacao|Cocoa|Cacao|Cacau|Cacao|Kakao|可可棕|ココア|코코아|Какао
ink|Inchiostro|Ink|Tinta|Tinta|Encre|Tinte|墨黑|インク|잉크|Чернила
share_image|Condividi l’immagine|Share image|Compartir imagen|Compartilhar imagem|Partager l’image|Bild teilen|分享图片|画像を共有|이미지 공유|Поделиться изображением
save_png|Salva immagine PNG|Save PNG image|Guardar imagen PNG|Salvar imagem PNG|Enregistrer en PNG|PNG-Bild speichern|保存 PNG 图片|PNG画像を保存|PNG 이미지 저장|Сохранить PNG
save_svg|Salva vettoriale SVG|Save SVG vector|Guardar vector SVG|Salvar vetor SVG|Enregistrer en SVG|SVG-Vektorgrafik speichern|保存 SVG 矢量图|SVG形式で保存|SVG 벡터 저장|Сохранить SVG
share_text|Condividi il contenuto|Share content|Compartir contenido|Compartilhar conteúdo|Partager le contenu|Inhalt teilen|分享内容|内容を共有|내용 공유|Поделиться содержимым
rename|Rinomina|Rename|Cambiar nombre|Renomear|Renommer|Umbenennen|重命名|名前を変更|이름 변경|Переименовать
delete|Elimina|Delete|Eliminar|Excluir|Supprimer|Löschen|删除|削除|삭제|Удалить
delete_confirm|Eliminare questo codice?|Delete this code?|¿Eliminar este código?|Excluir este código?|Supprimer ce code ?|Diesen Code löschen?|删除此代码？|このコードを削除しますか？|이 코드를 삭제할까요?|Удалить этот код?
open_link|Apri il collegamento|Open link|Abrir enlace|Abrir link|Ouvrir le lien|Link öffnen|打开链接|リンクを開く|링크 열기|Открыть ссылку
connect_wifi|Apri impostazioni Wi-Fi|Open Wi-Fi settings|Abrir ajustes de Wi-Fi|Abrir configurações de Wi-Fi|Ouvrir les réglages Wi-Fi|WLAN-Einstellungen öffnen|打开 Wi-Fi 设置|Wi-Fi設定を開く|Wi-Fi 설정 열기|Настройки Wi-Fi
add_contact|Aggiungi ai contatti|Add to contacts|Añadir a contactos|Adicionar aos contatos|Ajouter aux contacts|Zu Kontakten hinzufügen|添加联系人|連絡先に追加|연락처에 추가|Добавить контакт
add_event|Aggiungi al calendario|Add to calendar|Añadir al calendario|Adicionar ao calendário|Ajouter à l’agenda|Zum Kalender hinzufügen|添加到日历|カレンダーに追加|캘린더에 추가|Добавить в календарь
open_map|Apri nella mappa|Open in maps|Abrir en el mapa|Abrir no mapa|Ouvrir la carte|In Karte öffnen|在地图中打开|地図で開く|지도에서 열기|Открыть на карте
dial|Apri il telefono|Open dialer|Abrir teléfono|Abrir telefone|Ouvrir le téléphone|Wählfeld öffnen|打开拨号界面|ダイヤル画面を開く|전화 앱 열기|Открыть набор номера
compose|Prepara il messaggio|Compose message|Redactar mensaje|Escrever mensagem|Rédiger le message|Nachricht verfassen|编写消息|メッセージを作成|메시지 작성|Написать сообщение
wifi_connect_hint|La password verrà copiata. Seleziona la rete nelle impostazioni Wi-Fi e incollala.|The password will be copied. Select the network in Wi-Fi settings and paste it.|Se copiará la contraseña. Selecciona la red en los ajustes de Wi-Fi y pégala.|A senha será copiada. Selecione a rede nas configurações de Wi-Fi e cole a senha.|Le mot de passe sera copié. Choisissez le réseau dans les réglages Wi-Fi et collez-le.|Das Passwort wird kopiert. Wähle das Netzwerk in den WLAN-Einstellungen und füge es ein.|密码将被复制。请在 Wi-Fi 设置中选择网络并粘贴密码。|パスワードをコピーします。Wi-Fi設定でネットワークを選び、貼り付けてください。|비밀번호가 복사됩니다. Wi-Fi 설정에서 네트워크를 선택하고 붙여 넣으세요.|Пароль будет скопирован. Выберите сеть в настройках Wi-Fi и вставьте его.
open_confirm|Controlla il contenuto prima di aprirlo in un’altra app.|Check the content before opening it in another app.|Revisa el contenido antes de abrirlo en otra app.|Confira o conteúdo antes de abrir em outro app.|Vérifiez le contenu avant de l’ouvrir dans une autre application.|Prüfe den Inhalt, bevor du ihn in einer anderen App öffnest.|在其他应用中打开前，请先检查内容。|別のアプリで開く前に内容を確認してください。|다른 앱에서 열기 전에 내용을 확인하세요.|Проверьте содержимое перед открытием в другом приложении.
continue_action|Continua|Continue|Continuar|Continuar|Continuer|Weiter|继续|続ける|계속|Продолжить
settings_title|A modo tuo.|Make it yours.|A tu manera.|Do seu jeito.|À votre façon.|So, wie du es magst.|随你所好。|あなたらしく。|나에게 맞게.|По-вашему.
history_setting|Conserva la cronologia|Keep history|Guardar historial|Guardar histórico|Conserver l’historique|Verlauf speichern|保留历史记录|履歴を保存|기록 보관|Сохранять историю
history_hint|Salva automaticamente i nuovi codici sul telefono. I preferiti si salvano sempre.|Automatically save new codes on your phone. Favorites are always saved.|Guarda los nuevos códigos en el teléfono. Los favoritos siempre se guardan.|Salva novos códigos no celular. Os favoritos são sempre salvos.|Enregistre les nouveaux codes sur le téléphone. Les favoris sont toujours conservés.|Neue Codes automatisch auf dem Gerät speichern. Favoriten werden immer gespeichert.|自动在手机上保存新代码。收藏的代码始终会保存。|新しいコードを端末に自動保存します。お気に入りは常に保存されます。|새 코드를 휴대전화에 자동 저장해요. 즐겨찾기는 항상 저장돼요.|Новые коды сохраняются на телефоне. Избранное сохраняется всегда.
vibrate_setting|Vibra alla scansione|Vibrate on scan|Vibrar al escanear|Vibrar ao escanear|Vibrer lors du scan|Beim Scannen vibrieren|扫描时振动|読み取り時に振動|스캔 시 진동|Вибрация при сканировании
vibrate_hint|Un piccolo segnale quando viene riconosciuto un codice.|A gentle signal when a code is recognized.|Una señal suave al reconocer un código.|Um sinal suave quando um código é reconhecido.|Un signal discret lorsqu’un code est reconnu.|Ein dezentes Signal, wenn ein Code erkannt wird.|识别到代码时轻轻振动提示。|コードを読み取ったら、軽い振動でお知らせします。|코드를 인식하면 가벼운 진동으로 알려요.|Лёгкий сигнал при распознавании кода.
privacy_title|I tuoi codici restano tuoi.|Your codes stay yours.|Tus códigos siguen siendo tuyos.|Seus códigos continuam seus.|Vos codes restent les vôtres.|Deine Codes bleiben bei dir.|你的代码，由你掌握。|コードはあなたのもの。|내 코드는 내 손안에.|Ваши коды — только ваши.
privacy_body|Scansione e creazione offline. Nessun account, pubblicità o tracciamento. La fotocamera serve solo a leggere i codici; le immagini non vengono conservate. La cronologia resta nell’app e puoi eliminarla. Condivisioni e app esterne sono una tua scelta.|Scanning and creation work offline. No account, ads or tracking. The camera is only used to read codes; images are not stored. History stays in the app and can be deleted. Sharing and external apps are your choice.|Escaneo y creación sin conexión. Sin cuenta, anuncios ni rastreo. La cámara solo lee códigos; las imágenes no se guardan. El historial permanece en la app y puedes borrarlo. Tú decides si compartir o abrir otras apps.|Leitura e criação offline. Sem conta, anúncios ou rastreamento. A câmera só lê códigos; as imagens não são guardadas. O histórico fica no app e pode ser excluído. Você decide se quer compartilhar ou abrir outros apps.|Scan et création hors ligne. Sans compte, publicité ni suivi. La caméra lit les codes sans conserver les images. L’historique reste dans l’application et peut être supprimé. Vous choisissez de partager ou d’ouvrir une autre application.|Scannen und Erstellen ohne Internet. Kein Konto, keine Werbung, kein Tracking. Die Kamera liest nur Codes; Bilder werden nicht gespeichert. Der Verlauf bleibt in der App und lässt sich löschen. Du entscheidest über Teilen und externe Apps.|扫描和创建均可离线进行。无需账号，无广告，无追踪。摄像头仅用于读取代码，不保存拍摄画面。历史记录保存在应用内，可随时删除。分享或打开外部应用均由你选择。|スキャンも作成もオフライン。アカウント、広告、追跡はありません。カメラはコードの読み取りだけに使い、映像は保存しません。履歴はアプリ内に保存され、削除できます。共有や外部アプリの利用は自分で選べます。|스캔과 생성은 오프라인으로 작동해요. 계정, 광고, 추적이 없어요. 카메라는 코드 읽기에만 사용하며 촬영 영상은 저장하지 않아요. 기록은 앱 안에 보관되며 삭제할 수 있어요. 공유와 외부 앱 사용은 직접 선택해요.|Сканирование и создание работают офлайн. Без аккаунта, рекламы и отслеживания. Камера только читает коды; изображения не сохраняются. История хранится в приложении, её можно удалить. Вы сами решаете, чем делиться и что открывать в других приложениях.
export_history|Esporta la cronologia|Export history|Exportar historial|Exportar histórico|Exporter l’historique|Verlauf exportieren|导出历史记录|履歴を書き出す|기록 내보내기|Экспорт истории
export_history_hint|Il file JSON include il contenuto dei codici, anche eventuali password Wi-Fi. Scegli dove conservarlo.|The JSON file includes code contents, including any Wi-Fi passwords. Choose where to keep it.|El archivo JSON incluye el contenido de los códigos y las contraseñas Wi-Fi que contengan. Elige dónde guardarlo.|O arquivo JSON inclui o conteúdo dos códigos, inclusive senhas de Wi-Fi. Escolha onde guardá-lo.|Le fichier JSON contient les codes, y compris leurs éventuels mots de passe Wi-Fi. Choisissez où le conserver.|Die JSON-Datei enthält die Code-Inhalte, auch mögliche WLAN-Passwörter. Wähle einen Speicherort.|JSON 文件包含代码内容，包括其中的 Wi-Fi 密码。请选择保存位置。|JSONファイルにはWi-Fiパスワードを含むコードの内容が保存されます。保存先を選んでください。|JSON 파일에는 Wi-Fi 비밀번호를 포함한 코드 내용이 저장돼요. 저장할 위치를 선택하세요.|JSON-файл содержит данные кодов, в том числе пароли Wi-Fi. Выберите место хранения.
clear_history|Svuota la raccolta|Clear collection|Vaciar colección|Limpar coleção|Vider la collection|Sammlung leeren|清空收藏库|コレクションを消去|보관함 비우기|Очистить коллекцию
clear_confirm|Eliminare tutti i codici salvati, compresi i preferiti? I file esportati resteranno dove li hai salvati.|Delete all saved codes, including favorites? Exported files will remain where you saved them.|¿Eliminar todos los códigos, incluidos los favoritos? Los archivos exportados permanecerán donde los guardaste.|Excluir todos os códigos, inclusive favoritos? Os arquivos exportados continuarão onde você os salvou.|Supprimer tous les codes, y compris les favoris ? Les fichiers exportés resteront à leur emplacement.|Alle gespeicherten Codes einschließlich Favoriten löschen? Exportierte Dateien bleiben erhalten.|删除所有已保存的代码，包括收藏？已导出的文件仍保留在原位置。|お気に入りを含むすべてのコードを削除しますか？書き出したファイルは削除されません。|즐겨찾기를 포함한 모든 코드를 삭제할까요? 내보낸 파일은 원래 위치에 남아요.|Удалить все коды, включая избранное? Экспортированные файлы останутся на своих местах.
deleted|Raccolta svuotata|Collection cleared|Colección vaciada|Coleção limpa|Collection vidée|Sammlung geleert|收藏库已清空|コレクションを消去しました|보관함을 비웠어요|Коллекция очищена
about|Leggi QR · 2.1.1|Leggi QR · 2.1.1|Leggi QR · 2.1.1|Leggi QR · 2.1.1|Leggi QR · 2.1.1|Leggi QR · 2.1.1|Leggi QR · 2.1.1|Leggi QR · 2.1.1|Leggi QR · 2.1.1|Leggi QR · 2.1.1
about_body|Creata da zero per Android. Un’interfaccia leggera, 40 lingue e spazio per le cose semplici.|Built from scratch for Android. A light interface, 40 languages and room for simple things.|Creada desde cero para Android. Una interfaz ligera y 40 idiomas.|Criado do zero para Android. Uma interface leve e 40 idiomas.|Créée de zéro pour Android. Une interface légère et 40 langues.|Von Grund auf für Android entwickelt. Eine leichte Oberfläche und 40 Sprachen.|专为 Android 从零打造。轻巧的界面，支持40种语言。|Android向けに一から開発。軽やかな画面と40言語に対応。|Android용으로 처음부터 만들었어요. 가벼운 화면과 40개 언어를 지원해요.|Создано с нуля для Android. Лёгкий интерфейс и 40 языков.
licenses|Licenze delle librerie|Library licenses|Licencias de las bibliotecas|Licenças das bibliotecas|Licences des bibliothèques|Bibliothekslizenzen|第三方库许可|ライブラリのライセンス|라이브러리 라이선스|Лицензии библиотек
scan_hint|Inquadra il codice. Tocca per mettere a fuoco.|Frame the code. Tap to focus.|Encuadra el código. Toca para enfocar.|Enquadre o código. Toque para focar.|Cadrez le code. Touchez pour faire la mise au point.|Code ins Bild nehmen. Zum Fokussieren tippen.|将代码放入框内，轻触对焦。|コードを枠に合わせ、タップでピントを調整。|코드를 화면에 맞추고 탭하여 초점을 맞추세요.|Наведите камеру. Коснитесь экрана для фокусировки.
zoom|Zoom|Zoom|Zoom|Zoom|Zoom|Zoom|缩放|ズーム|확대|Масштаб
flash|Torcia|Flashlight|Linterna|Lanterna|Lampe|Taschenlampe|手电筒|ライト|손전등|Фонарик
flash_on|Torcia accesa|Flashlight on|Linterna encendida|Lanterna ligada|Lampe allumée|Taschenlampe an|手电筒已开启|ライトをオンにしました|손전등 켜짐|Фонарик включён
flash_off|Torcia spenta|Flashlight off|Linterna apagada|Lanterna desligada|Lampe éteinte|Taschenlampe aus|手电筒已关闭|ライトをオフにしました|손전등 꺼짐|Фонарик выключен
switch_camera|Cambia camera|Switch camera|Cambiar cámara|Trocar câmera|Changer de caméra|Kamera wechseln|切换摄像头|カメラ切替|카메라 전환|Сменить камеру
batch|Scansione multipla|Batch scanning|Escaneo múltiple|Leitura múltipla|Scan multiple|Mehrere Codes scannen|批量扫描|連続スキャン|연속 스캔|Пакетное сканирование
batch_count|Codici raccolti:|Codes collected:|Códigos recogidos:|Códigos coletados:|Codes collectés :|Erfasste Codes:|已扫描代码数：|読み取り済み：|스캔한 코드:|Собрано кодов:
camera_permission|Consenti l’uso della fotocamera|Allow camera access|Permitir acceso a la cámara|Permitir acesso à câmera|Autoriser la caméra|Kamerazugriff erlauben|允许使用摄像头|カメラへのアクセスを許可|카메라 접근 허용|Разрешите доступ к камере
permission_hint|Per scansionare, abilita la fotocamera nelle impostazioni dell’app. Puoi sempre importare un’immagine dalla schermata iniziale.|Enable the camera in app settings to scan. You can still import images from the home screen.|Activa la cámara en los ajustes de la app para escanear. También puedes importar imágenes desde el inicio.|Ative a câmera nas configurações do app para escanear. Você também pode importar imagens pela tela inicial.|Activez la caméra dans les réglages de l’application. Vous pouvez toujours importer une image depuis l’accueil.|Aktiviere die Kamera in den App-Einstellungen. Bilder kannst du weiterhin auf der Startseite importieren.|请在应用设置中允许摄像头权限以进行扫描。你也可以从首页导入图片。|スキャンするにはアプリ設定でカメラを許可してください。ホーム画面から画像を読み込むこともできます。|스캔하려면 앱 설정에서 카메라를 허용하세요. 홈 화면에서 이미지를 가져올 수도 있어요.|Для сканирования разрешите камеру в настройках приложения. Изображения можно импортировать с главного экрана.
camera_error|Fotocamera non disponibile. Torna indietro e riprova, oppure importa un’immagine.|Camera unavailable. Go back and try again, or import an image.|Cámara no disponible. Vuelve atrás e inténtalo de nuevo o importa una imagen.|Câmera indisponível. Volte e tente novamente ou importe uma imagem.|Caméra indisponible. Revenez en arrière et réessayez, ou importez une image.|Kamera nicht verfügbar. Gehe zurück und versuche es erneut oder importiere ein Bild.|摄像头不可用。请返回后重试，或导入图片。|カメラを利用できません。戻って再試行するか、画像を読み込んでください。|카메라를 사용할 수 없어요. 돌아가서 다시 시도하거나 이미지를 가져오세요.|Камера недоступна. Вернитесь и попробуйте снова или импортируйте изображение.
unavailable|Funzione non disponibile su questo dispositivo.|This feature is unavailable on this device.|Esta función no está disponible en este dispositivo.|Este recurso não está disponível neste dispositivo.|Cette fonction n’est pas disponible sur cet appareil.|Diese Funktion ist auf diesem Gerät nicht verfügbar.|此设备不支持此功能。|この端末では利用できません。|이 기기에서는 사용할 수 없는 기능이에요.|Эта функция недоступна на устройстве.
no_code|Nessun codice riconosciuto. Prova un’immagine più nitida.|No code recognized. Try a clearer image.|No se reconoció ningún código. Prueba una imagen más nítida.|Nenhum código reconhecido. Tente uma imagem mais nítida.|Aucun code reconnu. Essayez une image plus nette.|Kein Code erkannt. Versuche ein schärferes Bild.|未识别到代码，请尝试更清晰的图片。|コードを認識できませんでした。鮮明な画像をお試しください。|코드를 인식하지 못했어요. 더 선명한 이미지를 사용해 보세요.|Код не распознан. Попробуйте более чёткое изображение.
choose_code|Scegli un codice|Choose a code|Elige un código|Escolha um código|Choisissez un code|Code auswählen|选择代码|コードを選択|코드 선택|Выберите код
reading_image|Lettura dell’immagine…|Reading image…|Leyendo imagen…|Lendo imagem…|Lecture de l’image…|Bild wird gelesen…|正在识别图片…|画像を読み取り中…|이미지 읽는 중…|Чтение изображения…
image_error|Impossibile leggere questa immagine. Prova un file PNG o JPEG.|Cannot read this image. Try a PNG or JPEG file.|No se puede leer esta imagen. Prueba un archivo PNG o JPEG.|Não foi possível ler a imagem. Tente um arquivo PNG ou JPEG.|Impossible de lire cette image. Essayez un fichier PNG ou JPEG.|Dieses Bild kann nicht gelesen werden. Versuche eine PNG- oder JPEG-Datei.|无法读取此图片。请尝试 PNG 或 JPEG 文件。|画像を読み込めません。PNGまたはJPEGをお試しください。|이미지를 읽을 수 없어요. PNG 또는 JPEG 파일을 사용해 보세요.|Не удалось прочитать изображение. Попробуйте PNG или JPEG.
export_error|Esportazione non riuscita. Controlla lo spazio disponibile o scegli un’altra cartella.|Export failed. Check available space or choose another folder.|No se pudo exportar. Revisa el espacio disponible o elige otra carpeta.|Falha ao exportar. Verifique o espaço ou escolha outra pasta.|Échec de l’exportation. Vérifiez l’espace disponible ou choisissez un autre dossier.|Export fehlgeschlagen. Prüfe den Speicherplatz oder wähle einen anderen Ordner.|导出失败。请检查可用空间或选择其他文件夹。|書き出せませんでした。空き容量を確認するか、別のフォルダを選んでください。|내보내지 못했어요. 저장 공간을 확인하거나 다른 폴더를 선택하세요.|Не удалось экспортировать. Проверьте свободное место или выберите другую папку.
saved|File salvato|File saved|Archivo guardado|Arquivo salvo|Fichier enregistré|Datei gespeichert|文件已保存|ファイルを保存しました|파일을 저장했어요|Файл сохранён
copied|Copiato negli appunti|Copied to clipboard|Copiado al portapapeles|Copiado para a área de transferência|Copié dans le presse-papiers|In die Zwischenablage kopiert|已复制到剪贴板|クリップボードにコピーしました|클립보드에 복사했어요|Скопировано в буфер обмена
no_handler|Nessuna app disponibile per questa azione.|No app available for this action.|No hay ninguna app para esta acción.|Nenhum app disponível para esta ação.|Aucune application disponible pour cette action.|Keine App für diese Aktion verfügbar.|没有可执行此操作的应用。|この操作に対応するアプリがありません。|이 작업을 처리할 앱이 없어요.|Нет приложения для этого действия.
'''

# Passo 1: risolvi il progetto dalla posizione dello script, senza percorsi del PC dell’autore.
here = Path(__file__).resolve().parent
project = here.parent
res = project / 'app/src/main/res'
languages = ['it', 'en', 'es', 'pt-rBR', 'fr', 'de', 'b+zh+Hans', 'ja', 'ko', 'ru']

# Passo 2: controlla numero di colonne, chiavi duplicate e celle vuote prima di scrivere.
rows = [line.split('|') for line in DATA.strip().splitlines()]
assert all(len(row) == 11 and all(cell.strip() for cell in row) for row in rows)
assert len({row[0] for row in rows}) == len(rows)

# Passo 3: leggi la versione da Gradle, unica sorgente del numero di versione dell’app.
import re
version = re.search(r"versionName '([^']+)'", (project/'app/build.gradle').read_text(encoding='utf-8')).group(1)
for row in rows:
    if row[0] == 'about':
        row[1:] = [f'Leggi QR · {version} · GPLv3'] * len(languages)

# Passo 4: genera risorse Android con escape XML e apostrofi compatibili con aapt2.
for column, language in enumerate(languages):
    path = res / ('values' if language == 'en' else 'values-' + language) / 'strings.xml'
    path.parent.mkdir(parents=True, exist_ok=True)
    lines = ['<?xml version="1.0" encoding="utf-8"?>',
             '<!-- SPDX-License-Identifier: GPL-3.0-only; generato da tools/generate_translations.py -->',
             '<resources>']
    if language == 'en':
        lines.append('    <string name="app_name" translatable="false">Leggi QR</string>')
    for row in rows:
        value = escape(row[column + 1]).replace("'", r"\'").replace('"', r'\"')
        lines.append(f'    <string name="{row[0]}" formatted="false">{value}</string>')
    path.write_text('\n'.join(lines + ['</resources>']) + '\n', encoding='utf-8')

# Passo 5: conserva le stringhe richiamate per chiave anche nella release ottimizzata.
(res/'raw').mkdir(exist_ok=True)
(res/'raw/keep.xml').write_text('<resources xmlns:tools="http://schemas.android.com/tools" tools:keep="@string/*" />', encoding='utf-8')

# Passo 6: trasforma le chiavi in riferimenti R controllati dal compilatore Java.
header = '/*\n * Leggi QR — Copyright (C) 2026 devfaxpro\n * SPDX-License-Identifier: GPL-3.0-only\n * Il testo completo della licenza è nel file LICENSE.\n */\n'
java = res.parent/'java/app/qrall/scanner/Strings.java'
body = ''.join('      case "'+row[0]+'" -> R.string.'+row[0]+';\n' for row in rows)
java.write_text(header + 'package app.qrall.scanner;\n\n'
    + '/** Mappa generata dalle traduzioni: modificare il catalogo, non questo file. */\n'
    + 'final class Strings {\n'
    + '  /** Risolve la chiave; una chiave sconosciuta è un errore del programma. */\n'
    + '  static int id(String key) {\n    return switch (key) {\n'
    + body + '      default -> throw new IllegalArgumentException("Unknown text resource: " + key);\n'
    + '    };\n  }\n}\n', encoding='utf-8')
print(f'{len(rows)} strings validated in the original {len(languages)} languages.')

# Passo 7: completa le altre trenta lingue e verifica insieme tutte le quaranta.
import runpy
runpy.run_path(str(here/'generate_locales.py'), run_name='__main__')
