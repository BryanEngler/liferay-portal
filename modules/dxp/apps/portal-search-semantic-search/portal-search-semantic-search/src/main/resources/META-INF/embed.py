import tensorflow_hub as hub

embed = hub.load("https://tfhub.dev/google/universal-sentence-encoder/4")

print("Google Universal Sentence Encoder Loaded")

while True:
  var = input()
  embeddings = embed([var])
  print(embeddings)