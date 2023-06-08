import os
import sklearn as sk
import matplotlib.pyplot as plt
import datetime
import numpy as np

import tensorflow as tf
from tensorflow import keras
from instancenormalization import InstanceNormalization

import CycleGAN_model
from CycleGAN_model import define_generator, define_discriminator, define_composite_model, train

gpus = tf.config.experimental.list_physical_devices('GPU')
if gpus:
    try:
        # Restrict TensorFlow to only use the first GPU
        tf.config.experimental.set_visible_devices(gpus[0], 'GPU')

        # Set memory limit to 3 GB on the first GPU
        tf.config.experimental.set_virtual_device_configuration(
            gpus[0],
            [tf.config.experimental.VirtualDeviceConfiguration(memory_limit=4096)]
        )

        # Enable memory growth to allocate only required memory
        # tf.config.experimental.set_memory_growth(gpus[0], True)

        print("TensorFlow is limited to 3 GB memory on the first GPU.")
    except RuntimeError as e:
        print(e)

def load_images(path, size=(256, 256)):
    data_list = list()
    # enumerate filenames in directory, assume all are images
    for filename in os.listdir(path):
        # load and resize the image
        pixels = keras.preprocessing.image.load_img(path + filename, target_size=size)
        # Convert to numpy array
        pixels = keras.preprocessing.image.img_to_array(pixels)
        # Store
        data_list.append(pixels)

    return np.asarray(data_list)

# Dataset path
path = './datasets/'

# Load dataset A - Monet paintings
print('Loading data...')
dataA_all = load_images(path + 'train_img_small/')
print('Loaded dataA: ', dataA_all.shape)
# Get a random sample of 100 images, for faster training for demonstration
# dataA = sk.utils.resample(dataA_all,
#                           replace=False,
#                           n_samples=3000,
#                           random_state=42)

dataB_all = load_images(path + 'train_color_small/')
print('Loaded dataB: ', dataB_all.shape)
# dataB = sk.utils.resample(dataB_all,
#                           replace=False,
#                           n_samples=3000,
#                           random_state=42)

# plot source images
# n_samples = 3
# for i in range(n_samples):
#     plt.subplot(2, n_samples, 1 + i)
#     plt.axis('off')
#     plt.imshow(dataA[i].astype('uint8'))
#
# # plot target image
# for i in range(n_samples):
#     plt.subplot(2, n_samples, 1 + n_samples + i)
#     plt.axis('off')
#     plt.imshow(dataB[i].astype('uint8'))

# plt.show()

# load image data
# data = [dataA, dataB]
data = [dataA_all, dataB_all]
print('Loaded ', data[0].shape, data[1].shape)

# All data in np.float16 (memory heavy. slightly reduced)
# def preprocess_data(data):
#     X1, X2 = data[0], data[1]
#     # Scale from [0,255] to [-1,1] and convert to float16
#     X1 = ((X1.astype(np.float16) / 127.5) - 1.0)
#     X2 = ((X2.astype(np.float16) / 127.5) - 1.0)
#     return [X1, X2]

# All data in np.float32 (memory heavy)
def preprocess_data(data):
    X1, X2 = data[0], data[1]
    # scale from [0,255] to [-1,1]
    X1 = (X1 - 127.5) / 127.5
    X2 = (X2 - 127.5) / 127.5
    return [X1, X2]

print('Preprocessing data...')
dataset = preprocess_data(data)

# Define input shape based on the loaded dataset
image_shape = dataset[0].shape[1:]
# generator: A -> B
g_model_AtoB = define_generator(image_shape)
# generator: B -> A
g_model_BtoA = define_generator(image_shape)
# discriminator: A -> [real/fake]
d_model_A = define_discriminator(image_shape)
# discriminator: B -> [real/fake]
d_model_B = define_discriminator(image_shape)
# composite: A -> B -> [real/fake, A]
c_model_AtoB = define_composite_model(g_model_AtoB, d_model_B, g_model_BtoA, image_shape)
# composite: B -> A -> [real/fake, B]
c_model_BtoA = define_composite_model(g_model_BtoA, d_model_A, g_model_AtoB, image_shape)

# train models
print('Training models...')
start1 = datetime.datetime.now()

train(d_model_A, d_model_B, g_model_AtoB, g_model_BtoA, c_model_AtoB, c_model_BtoA, dataset, epochs=5)

stop1 = datetime.datetime.now()

# Execution time
execution_time = stop1-start1
print('Execution time: ', execution_time)

