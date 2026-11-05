# SPDX-License-Identifier: GPL-3.0-only
# Copyright (C) 2026 devfaxpro
"""Completa e verifica le 40 lingue; avviare tramite generate_translations.py."""
from pathlib import Path
import json
import re
import unicodedata
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape

here = Path(__file__).resolve().parent
project = here.parent if here.name == 'tools' else here / 'QrAllSoft'
res = project / 'app/src/main/res'
# Passo 1: usa la stessa versione dichiarata per APK e AAB.
version = re.search(r"versionName '([^']+)'", (project/'app/build.gradle').read_text(encoding='utf-8')).group(1)
# Passo 2: conserva le prime dieci lingue nell’ordine esplicitamente richiesto.
original = [('it','Italiano','it'), ('en','English','en'), ('es','Español','es'),
            ('pt-BR','Português (Brasil)','pt-rBR'), ('fr','Français','fr'),
            ('de','Deutsch','de'), ('zh-Hans','简体中文','b+zh+Hans'),
            ('ja','日本語','ja'), ('ko','한국어','ko'), ('ru','Русский','ru')]
added = [('nl','Nederlands'),('pl','Polski'),('cs','Čeština'),('hu','Magyar'),('ro','Română'),
         ('sv','Svenska'),('da','Dansk'),('fi','Suomi'),('nb','Norsk bokmål'),('el','Ελληνικά'),
         ('uk','Українська'),('bg','Български'),('tr','Türkçe'),('ar','العربية'),('he','עברית'),
         ('fa','فارسی'),('ur','اردو'),('hi','हिन्दी'),('bn','বাংলা'),('mr','मराठी'),
         ('ta','தமிழ்'),('te','తెలుగు'),('pa','ਪੰਜਾਬੀ'),('gu','ગુજરાતી'),('sw','Kiswahili'),
         ('id','Bahasa Indonesia'),('ms','Bahasa Melayu'),('vi','Tiếng Việt'),('th','ไทย'),('fil','Filipino')]
# Le altre lingue sono ordinate per nome nativo, ignorando accenti e maiuscole.
added.sort(key=lambda x: ''.join(c for c in unicodedata.normalize('NFKD',x[1]).casefold() if not unicodedata.combining(c)))
catalog = original + [(tag,name,{'he':'iw','id':'in'}.get(tag,tag)) for tag,name in added]
assert len(catalog) == 40 and len({tag.split('-')[0] for tag,_,_ in catalog}) == 40
base = ET.parse(res/'values/strings.xml').getroot()
keys = [s.attrib['name'] for s in base if s.attrib.get('translatable') != 'false']
expected = set(keys) - {'wifi','about'}
# Passo 3: controlla tutte le chiavi prima di generare le nuove risorse.
translations = {}
for path in sorted((here/'locales').glob('*.tsv')):
    rows = [line.split('|') for line in path.read_text(encoding='utf-8').splitlines() if line.strip()]
    tags = rows[0][1:]
    assert len(rows[0]) == 6 and len(tags) == len(set(tags)), path
    assert all(len(row) == len(tags)+1 and all(value.strip() for value in row) for row in rows[1:]), path
    rowkeys = [row[0] for row in rows[1:]]
    assert len(set(rowkeys)) == len(rowkeys), f'Duplicate keys: {path}'
    assert set(rowkeys) == expected, (path, expected-set(rowkeys),set(rowkeys)-expected)
    for i,tag in enumerate(tags):
        assert tag not in translations
        translations[tag] = {row[0]:row[i+1] for row in rows[1:]}
        translations[tag].update(wifi='Wi-Fi',about=f'Leggi QR · {version} · GPLv3')
assert set(translations) == {tag for tag,_ in added}

def android_text(value):
    """Protegge i caratteri XML e le virgolette senza perdere le scritture Unicode."""
    return escape(value).replace("'",r"\'").replace('"',r'\"')

# Passo 4: scrivi un file di risorse per ogni lingua aggiunta.
for tag,name,qualifier in catalog[10:]:
    folder = res / f'values-{qualifier}'
    folder.mkdir(exist_ok=True)
    lines=['<?xml version="1.0" encoding="utf-8"?>','<!-- SPDX-License-Identifier: GPL-3.0-only; generato da tools/generate_locales.py -->','<resources>']
    lines += [f'    <string name="{key}" formatted="false">{android_text(translations[tag][key])}</string>' for key in keys]
    (folder/'strings.xml').write_text('\n'.join(lines+['</resources>'])+'\n',encoding='utf-8')

# Passo 5: allinea il selettore Android e i filtri della build allo stesso catalogo.
locale_config = ['<?xml version="1.0" encoding="utf-8"?>', '<locale-config xmlns:android="http://schemas.android.com/apk/res/android">']
locale_config += [f'    <locale android:name="{tag}" />' for tag,_,_ in catalog]
(res/'xml/locales_config.xml').write_text('\n'.join(locale_config+['</locale-config>'])+'\n',encoding='utf-8')

gradle=project/'app/build.gradle'
source=gradle.read_text(encoding='utf-8')
source=re.sub(r'resourceConfigurations \+= \[.*?\]', 'resourceConfigurations += '+repr([q for _,_,q in catalog]),source)
gradle.write_text(source,encoding='utf-8')
java=project/'app/src/main/java/app/qrall/scanner/LanguageCatalog.java'
# Passo 6: genera codici e nomi paralleli, mantenendo lo stesso indice fra le due liste.
java.write_text('/*\n * Leggi QR — Copyright (C) 2026 devfaxpro\n * SPDX-License-Identifier: GPL-3.0-only\n * Il testo completo della licenza è nel file LICENSE.\n */\n' + 'package app.qrall.scanner;\n\n'
    + '/** Catalogo generato: il selettore mostra NAMES, Android usa TAGS. */\n'
    + 'final class LanguageCatalog {\n'
    + '  // Codici interni BCP 47: non vengono mostrati alla persona.\n'
    + '  static final String[] TAGS = {\n'
    + ',\n'.join('    '+json.dumps(tag,ensure_ascii=False) for tag,_,_ in catalog) + '\n  };\n'
    + '  // Nomi nativi nello stesso ordine; il portoghese conserva la dicitura Brasil.\n'
    + '  static final String[] NAMES = {\n'
    + ',\n'.join('    '+json.dumps(name,ensure_ascii=False) for _,name,_ in catalog) + '\n  };\n}\n', encoding='utf-8')

# Passo 7: verifica tutti i file prodotti, comprese le dieci lingue iniziali.
for tag,name,q in catalog:
    file=res/('values' if tag=='en' else 'values-'+q)/'strings.xml'
    strings=ET.parse(file).getroot()
    localized={s.attrib['name']:s.text for s in strings if s.attrib.get('translatable') != 'false'}
    assert set(localized)==set(keys) and all(localized.values()),tag
    assert r'\n' in localized['hero_title'],tag
    assert version in localized['about'],tag
(project/'LANGUAGES.txt').write_text('Leggi QR — 40 lingue, 145 testi per lingua\n'
    'Scelta iniziale: lingua di sistema; se non disponibile, inglese. Scelta manuale: globo.\n'
    'Una voce per lingua, senza duplicati regionali. Portoghese brasiliano e cinese semplificato.\n'
    'Le dieci lingue concordate restano per prime, poi le altre ordinate per nome nativo.\n'
    'Copertura completa delle chiavi; revisione editoriale madrelingua non eseguita.\n\n'
    +'\n'.join(f'{n} — {t}' for t,n,_ in catalog)+'\n',encoding='utf-8')
print(f'Validated {len(keys)} strings x {len(catalog)} locales; no missing or empty keys.')
