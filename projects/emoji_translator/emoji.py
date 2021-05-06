dict = {}

def map_update(dict, st, emoji):
    sa = st.split()
    emoarray = emoji.split()
    for i in range(len(sa)):
        if sa[i] == emoarray[i]:
            continue
        else:
            dict[sa[i]] = emoarray[i]


def main():
    global dict
    map_update(dict,
               "She counted. One. She could hear the steps coming closer. Two. Puffs of breath could be seen coming from his mouth. Three. He stopped beside her. Four. She pulled the trigger of the gun.",
               "👤👩 counted. 1️⃣. 👤👩 could 👂️ the 🪜 ⤵️ closer. 2️⃣. Puffs of 🫁 could be 👀 ⤵️ from 👤👨⬅️ 👄. 3️⃣. 👤👨 🛑 beside 👤👩⬅️. 4️⃣. 👤👩 pulled the trigger of the 🔫.")
    map_update(dict, "The red glint of paint sparkled under the sun. He had dreamed of owning this car since he was ten, and that dream had become a reality less than a year ago. It was his baby and he spent hours caring for it, pampering it, and fondling over it. She knew this all too well, and that's exactly why she had taken a sludge hammer to it.", "The 🔴 glint of paint sparkled under the ☀️. 👤👨 had dreamed of owning this 🚘️ since 👤👨 was ten, ➕ that dream had become a reality less than a year ago. It was 👤👨⬅️ 👶 ➕ 👤👨 spent ° 🚘️ for it, pampering it, ➕ fondling over it. 👤👩 💡 this all too well, ➕ that's ≡ ❓️ 👤👩 had taken a sludge 🔨 to it.")
    map_update(dict, "You can decide what you want to do in life, but I suggest doing something that creates. Something that leaves a tangible thing once you're done. That way even after you're gone, you will still live on in the things you created.", "➡️👤 🥫 decide what ➡️👤 🙏 to do in 🧬, but I suggest doing something that creates. Something that 🍃 a tangible thing 🔂 you're ⌛️. That way even after you're ➡️, ➡️👤 will still live on in the things ➡️👤 created.")
    map_update(dict, "area book business case child company country day eye fact family government group hand home job life lot man money month mother Mr night number part people place point problem program question right room school state story student study system thing time waterway week woman word work world year", "area 📚️ 🧑‍💼 case 🧒 company country day 👁️ fact 👪️ government group ✋ 🏠️ job 🧬 lot 👤👨 💸 month 🤶 Mr 🌃 number § 👥 place 👈️ problem program ❓️ ➡️ room 🏫 state story 👨‍🎓️ study system thing ⏱️ waterway week 👤👩 word ⚙️ 🗺️ year")
    map_update(dict, "cat dog mouse elephant tiger water bottle cup beer wine cookies cookie folder folders candy party parties balloons balloon", "🐱 🐶 🐭 🐘 🐯 🌊 🍾 🥤 🍺 🍷 🍪 🍪 📁 📁 🍬 🎉 👯‍♂️ 🎈 🎈")
    map_update(dict, "beach sea boat house pizza fish dolphin war sword duck chicken home building school girl boy face ear nose eye eyes", "🏖️ ⛵️ 🛥️ 🏘️ 🍕 🎣 🐬 war 🤺 🦆 🐔 🏠️ 🏢 🏫 👧 👦 😀 👂️ 👃 👁️ 👀")

