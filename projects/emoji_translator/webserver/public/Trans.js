function Trans(text, lang) {
    return func(text, lang);
}

async function func(text, lang) {
    translate.engine = 'libre';
    const translatedText = await translate(text, lang);
    console.log(translateText)
    return translatedText;
}