from instancenormalization import InstanceNormalization
import tensorflow as tf
from tensorflow import keras
from keras.models import load_model
from PIL import Image

import matplotlib.pyplot as plt
import numpy as np

from CycleGAN_model import load_real_samples

def preprocess_image(image):
    image = keras.preprocessing.image.img_to_array(image)
    image = keras.preprocessing.image.smart_resize(image, (256, 256))
    image = np.expand_dims(image, axis=0)
    image = (image - 127.5) / 127.5
    return image

model_genA2B = load_model('./saved_models/gen_model_AtoB_020000.h5', custom_objects={'InstanceNormalization': InstanceNormalization})
model_genA2B.compile(optimizer='adam', loss='mse', metrics=['accuracy'])

model_genB2A = load_model('./saved_models/gen_model_BtoA_020000.h5', custom_objects={'InstanceNormalization': InstanceNormalization})
model_genB2A.compile(optimizer='adam', loss='mse', metrics=['accuracy'])

image_dir = '000010_0.jpg'
input_image = Image.open(image_dir)
input_image = preprocess_image(input_image)

monet_dir = '000028_1.jpg'
monet_image = Image.open(monet_dir)
monet_image = preprocess_image(monet_image)

# Generate image from input image
print('Generating image...')

gen_monet = model_genA2B.predict(input_image)
gen_image = model_genB2A.predict(monet_image)

gen_monet = Image.fromarray((gen_monet[0] * 127.5 + 127.5).astype(np.uint8))
gen_image = Image.fromarray((gen_image[0] * 127.5 + 127.5).astype(np.uint8))

gen_monet.save('gen_model.jpg')
gen_image.save('gen_cloth.jpg')