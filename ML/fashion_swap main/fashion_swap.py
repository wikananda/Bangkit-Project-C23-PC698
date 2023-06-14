from instancenormalization import InstanceNormalization
import tensorflow as tf
from tensorflow import keras
from keras.models import load_model
from PIL import Image

import io
import cv2
import matplotlib.pyplot as plt
import numpy as np
import base64

from flask import Flask, request, jsonify

model_Color2Edge = load_model('./model/gen_model_ColortoEdge_028440.h5', custom_objects={'InstanceNormalization': InstanceNormalization})
model_Color2Edge.compile(optimizer='adam', loss='mse', metrics=['accuracy'])

model_Edge2Segm = load_model('./model/gen_model_EdgetoSegm_020280.h5', custom_objects={'InstanceNormalization': InstanceNormalization})
model_Edge2Segm.compile(optimizer='adam', loss='mse', metrics=['accuracy'])

model_Segm2Img = load_model('./model/gen_model_SegmtoImg_020280.h5', custom_objects={'InstanceNormalization': InstanceNormalization})
model_Segm2Img.compile(optimizer='adam', loss=' mse', metrics=['accuracy'])


def preprocess_image(image):
    image = keras.preprocessing.image.img_to_array(image)
    image = keras.preprocessing.image.smart_resize(image, (64, 64))
    image = np.expand_dims(image, axis=0)
    image = (image - 127.5) / 127.5
    return image

def upscale_to_original(image, original_shape):
    image = np.asarray((image * 127.5 + 127.5).astype(np.uint8))
    print(original_shape[1], original_shape[0])
    image = cv2.resize(image, original_shape, interpolation=cv2.INTER_CUBIC)
    return image

def fashion_swap(model_image, cloth_image):
    # model_image = Image.open(model_dir)
    # cloth_image = Image.open(cloth_dir)

    model_original_shape = model_image.size
    cloth_original_shape = cloth_image.size

    print(model_original_shape)
    print(cloth_original_shape)

    input_model = preprocess_image(model_image)
    input_cloth = preprocess_image(cloth_image)

    gen_edge = model_Color2Edge.predict(input_cloth)
    gen_segm = model_Edge2Segm.predict(gen_edge)
    gen_image = model_Segm2Img.predict(gen_segm)

    gen_image = np.reshape(gen_image, (64, 64, 3))
    print(gen_image.shape)

    output_image = upscale_to_original(gen_image, model_original_shape)
    cv2.imwrite('output_image.jpg', output_image)
    return output_image

app = Flask(__name__)
@app.route("/", methods=["GET", "POST"])
def index():
    if request.method == "POST":
        model_file = request.files.get('model_file')
        cloth_file = request.files.get('cloth_file')
        if (model_file is None or model_file.filename == '') or (cloth_file is None or cloth_file.filename == ''):
            return jsonify({"error": "Missing file"})

        try :
            model_image = model_file.read()
            cloth_image = cloth_file.read()
            model_image = Image.open(io.BytesIO(model_image)).convert('RGB')
            cloth_image = Image.open(io.BytesIO(cloth_image)).convert('RGB')

            output_image = fashion_swap(model_image, cloth_image)
            output_image = base64.b64encode(output_image).decode('utf-8')

            return jsonify({"output_image": output_image})
        except Exception as e:
            return jsonify({"error": str(e)})

    return "OK"

if __name__ == "__main__":
    app.run(debug=True)