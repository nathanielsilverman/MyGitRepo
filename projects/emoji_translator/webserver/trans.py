from sys import argv

from deep_translator import GoogleTranslator
import emoji as e

e.main()
d = e.dict
print("im here")
dest_lang = argv[1].lower()
st = argv[2]

array = st.split()
for i in range(len(array)):
    if array[i] in d.keys():
        array[i] = d[array[i]]
s = " ".join(array)
translated = ''
try:
    translated = GoogleTranslator(source='auto', target=dest_lang).translate(s)
    print(translated)
except:
    print("Sorry, I did not get that")
return translated