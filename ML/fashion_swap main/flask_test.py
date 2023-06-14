import requests

files = {"model_file": open('000001_0.jpg','rb'), "cloth_file": open('000004_1.jpg','rb')}
resp = requests.post("http://localhost:5000/", files=files)

print(resp.json())