from random import random
import numpy as np
from numpy.random import randint

import tensorflow as tf
from tensorflow import keras
from keras import layers
from keras.optimizers import Adam
from keras.initializers import RandomNormal

from instancenormalization import InstanceNormalization

from matplotlib import pyplot as plt

# Discriminator as PatchGAN 70x70
def define_discriminator(image_shape):
    # weight initialization
    init = RandomNormal(stddev=0.02, seed=123)
    # source image input
    in_image = keras.Input(shape=image_shape)

    d = layers.Conv2D(64, (4, 4), strides=(2, 2), padding='same',
                      kernel_initializer=init)(in_image)
    d = layers.LeakyReLU(alpha=0.2)(d)

    d = layers.Conv2D(128, (4, 4), strides=(2, 2), padding='same',
                      kernel_initializer=init)(d)
    d = InstanceNormalization(axis=-1)(d)
    d = layers.LeakyReLU(alpha=0.2)(d)

    d = layers.Conv2D(256, (4, 4), strides=(2, 2), padding='same',
                      kernel_initializer=init)(d)
    d = InstanceNormalization(axis=-1)(d)
    d = layers.LeakyReLU(alpha=0.2)(d)

    d = layers.Conv2D(512, (4, 4), strides=(2, 2), padding='same',
                      kernel_initializer=init)(d)
    d = InstanceNormalization(axis=-1)(d)
    d = layers.LeakyReLU(alpha=0.2)(d)

    d = layers.Conv2D(512, (4, 4), padding='same',
                      kernel_initializer=init)(d)
    d = InstanceNormalization(axis=-1)(d)
    d = layers.LeakyReLU(alpha=0.2)(d)
    # patch output
    patch_out = layers.Conv2D(1, (4, 4), padding='same',
                              kernel_initializer=init)(d)
    model = keras.Model(in_image, patch_out)

    model.compile(loss='mse', optimizer=Adam(learning_rate=0.0002, beta_1=0.5), loss_weights=[0.5])
    return model

# generator a resnet block to be used in the generator
# resnet basically is : have several convolutional layers, take the output and concatenate it with the input
def resnet_block(n_filters, input_layer):
    # weight initialization
    init = RandomNormal(stddev=0.02, seed=123)

    # first convolutional layer
    g = layers.Conv2D(n_filters, (3, 3), padding='same', kernel_initializer=init)(input_layer)
    g = InstanceNormalization(axis=-1)(g)
    g = layers.Activation('relu')(g)

    # second convolutional layer
    g = layers.Conv2D(n_filters, (3, 3), padding='same', kernel_initializer=init)(g)
    g = InstanceNormalization(axis=-1)(g)

    # Concatenate merge channel-wise with input layer
    g = layers.Concatenate()([g, input_layer])
    return g

# define the  generator model - encoder-decoder type architecture

#c7s1-k denote a 7×7 Convolution-InstanceNorm-ReLU layer with k filters and stride 1.
#dk denotes a 3 × 3 Convolution-InstanceNorm-ReLU layer with k filters and stride 2.
# Rk denotes a residual block that contains two 3 × 3 convolutional layers with k filters
# uk denotes a 3 × 3 fractional-strided-Convolution InstanceNorm-ReLU layer with k filters and stride 1 or 2

#The network with 6 residual blocks consists of:
#c7s1-64,d128,d256,R256,R256,R256,R256,R256,R256,u128,u64,c7s1-3

#The network with 9 residual blocks consists of:
#c7s1-64,d128,d256,R256,R256,R256,R256,R256,R256,R256,R256,R256,u128, u64,c7s1-3
def define_generator(image_shape, n_resnet=9):
    # weight initialization
    init = RandomNormal(stddev=0.02, seed=123)

    in_image = keras.Input(shape=image_shape)

    # c7s1-64
    g = layers.Conv2D(64, (7,7), padding='same', kernel_initializer=init)(in_image)
    g = InstanceNormalization(axis=-1)(g)
    g = layers.Activation('relu')(g)

    # d128
    g = layers.Conv2D(128, (3,3), strides=(2,2), padding='same', kernel_initializer=init)(g)
    g = InstanceNormalization(axis=-1)(g)
    g = layers.Activation('relu')(g)

    # d256
    g = layers.Conv2D(256, (3,3), strides=(2,2), padding='same', kernel_initializer=init)(g)
    g = InstanceNormalization(axis=-1)(g)
    g = layers.Activation('relu')(g)

    # R256
    for _ in range(n_resnet):
        g = resnet_block(256, g)

    # u128
    g = layers.Conv2DTranspose(128, (3,3), strides=(2, 2), padding='same', kernel_initializer=init)(g)
    g = InstanceNormalization(axis=-1)(g)
    g = layers.Activation('relu')(g)

    # u64
    g = layers.Conv2DTranspose(64, (3, 3), strides=(2, 2), padding='same', kernel_initializer=init)(g)
    g = InstanceNormalization(axis=-1)(g)
    g = layers.Activation('relu')(g)

    # c7s1-3
    g = layers.Conv2D(3, (7, 7), padding='same', kernel_initializer=init)(g)
    g = InstanceNormalization(axis=-1)(g)
    out_image = layers.Activation('tanh')(g)

    # define model
    model = keras.Model(in_image, out_image)
    return model

# Define a composite model for updating generators by adversarial and cycle loss
# We define a composite model that will be used to train each generator separately.
def define_composite_model(gen_model_1, disc_model, gen_model_2, image_shape):
    # Make the generator of interset trainable as we will be updating these weights by keeping other models constant.
    gen_model_1.trainable = True
    # Mark discriminator and second generator as non-trainable
    disc_model.trainable = False
    gen_model_2.trainable = False

    # adversarial loss
    input_gen = keras.Input(shape=image_shape)
    gen1_out = gen_model_1(input_gen)
    output_disc = disc_model(gen1_out)

    # Identity Loss
    input_id = keras.Input(shape=image_shape)
    output_id = gen_model_1(input_id)

    # Cycle loss - forward
    output_forward = gen_model_2(gen1_out)

    # Cycle loss - backward
    gen2_out = gen_model_2(input_id)
    output_backward = gen_model_1(gen2_out)

    # Define model graph
    model = keras.Model([input_gen, input_id], [output_disc, output_id, output_forward, output_backward])

    # Define the optimizer
    opt = Adam(learning_rate=0.0002, beta_1=0.5)

    # Compile model with weighting of least squares loss and L1 loss
    model.compile(loss=['mse', 'mae', 'mae', 'mae'],
                  loss_weights=[1, 5, 10, 10], optimizer=opt)

    return model


def load_real_samples(filename):
    data = np.load(filename, allow_pickle=True)
    # unpack arrays
    X1, X2 = data['arr_0'], data['arr_1']

    # scale to [-1, 1]
    X1_min, X1_max = np.min(X1), np.max(X1)
    X2_min, X2_max = np.min(X2), np.max(X2)

    X1 = (X1 - X1_min) / (X1_max - X1_min)
    X2 = (X2 - X2_min) / (X2_max - X2_min)

    return [X1, X2]

# Discriminator needs 2 input, real input and fake input
# Real input is sampling from original data
# For real image, label is 1
def generate_real_samples(dataset, n_samples, patch_shape):
    # Choose random instances
    ix = randint(0, dataset.shape[0], n_samples)
    # Retrieve selected images
    X = dataset[ix]
    # Generate 'real' class labels (1)
    y = np.ones((n_samples, patch_shape, patch_shape, 1))
    return X,y

# Generate a batch of images, return images and targets
# For fake images, label is 0
def generate_fake_samples(gen_model, dataset, patch_shape):
    # Generate fake images
    X = gen_model.predict(dataset)
    # Create 'fake' class labels (0)
    y = np.zeros((len(X), patch_shape, patch_shape, 1))
    return X, y

# Periodically save the generator models to file
def save_models(step, gen_model_AtoB, gen_model_BtoA):
    # Save the first generator model
    directory1 = './saved_models/'
    filename1 = 'gen_model_AtoB_%06d.h5' % (step+1)
    gen_model_AtoB.save(directory1 + filename1)

    # Save the second generator model
    directory2 = './saved_models/'
    filename2 = 'gen_model_BtoA_%06d.h5' % (step+1)
    gen_model_AtoB.save(directory2 + filename2)

    print('>Saved: %s and %s' % (filename1, filename2))

def summarize_performance(step, gen_model, trainX, name, n_samples=5):
    # Select a sample of input images
    X_in, _ = generate_real_samples(trainX, n_samples, 0)
    # Generate translated images
    X_out, _ = generate_fake_samples(gen_model, X_in, 0)
    # Scale all pixels from [-1, 1] to [0, 1]
    X_in = (X_in + 1) / 2.0
    X_out = (X_out + 1) / 2.0

    # Plot real images
    for i in range(n_samples):
        plt.subplot(2, n_samples, 1 + i)
        plt.axis('off')
        plt.imshow(X_in[i])

    # Plot translated image
    for i in range(n_samples):
        plt.subplot(2, n_samples, 1 + n_samples + i)
        plt.axis('off')
        plt.imshow(X_out[i])

    # Save plot to file
    print(f'Saving plot {name}')
    directory = './summarized_performance/'
    filename = '%s_generated_plot_%06d.png' % (name, (step + 1))
    plt.savefig(directory + filename)
    plt.close()

# Update image pool for fake images to reduce model oscillation
# Update discriminators using a history of generated images rather than the ones produced by the latest generators.
# Original paper recommended keeping an image buffer that store the 50 previously created images
def update_image_pool(pool, images, max_size=50):
    selected = list()
    for image in images:
        if len(pool) < max_size:
            # Stock the pool
            pool.append(image)
            selected.append(image)
        elif random() < 0.5:
            # Use image, but don't add it to the pool
            selected.append(image)
        else:
            # Replace an existing image and use replaced image
            ix = randint(0, len(pool))
            selected.append(pool[ix])
            pool[ix] = image
    return np.asarray(selected)

# Train cycleGAN models
def train(d_model_A, d_model_B, g_model_AtoB, g_model_BtoA, c_model_AtoB, c_model_BtoA, dataset, epochs=1):
    # Define the properties of the training run
    n_epochs, n_batch, = epochs, 1 #batch size = 1 as suggested in the paper
    # determine the output square shape of the discriminator
    n_patch = d_model_A.output_shape[1]
    # unpack dataset
    trainA, trainB = dataset
    # Prepare image pool for fake images
    poolA, poolB = list(), list()
    # Calculate the number of batches per training epoch
    bat_per_epo = int(len(trainA) / n_batch)
    # Calculate the number of training iterations
    n_steps = bat_per_epo * n_epochs

    # Manually enumerate epochs
    for i in range(n_steps):
        # Generate (select) a batch of real samples from each domain (A and B)
        X_realA, y_realA = generate_real_samples(trainA, n_batch, n_patch)
        X_realB, y_realB = generate_real_samples(trainB, n_batch, n_patch)

        # Generate a batch of fake samples using both B to A and A to B generators
        X_fakeA, y_fakeA = generate_fake_samples(g_model_BtoA, X_realB, n_patch)
        X_fakeB, y_fakeB = generate_fake_samples(g_model_AtoB, X_realA, n_patch)

        # Update fake images in the pool. Paper suggest a buffer of 50 images
        X_fakeA = update_image_pool(poolA, X_fakeA)
        X_fakeB = update_image_pool(poolB, X_fakeB)

        # Update generator B -> A via the composite model
        g_loss2, _, _, _, _ = c_model_BtoA.train_on_batch([X_realB, X_realA], [y_realA, X_realA, X_realB, X_realA])
        # Update discriminator for A -> [real/fake]
        dA_loss1 = d_model_A.train_on_batch(X_realA, y_realA)
        dA_loss2 = d_model_A.train_on_batch(X_fakeA, y_fakeA)

        # Update generator A -> B via the composite model
        g_loss1, _, _, _, _ = c_model_AtoB.train_on_batch([X_realA, X_realB], [y_realB, X_realB, X_realA, X_realB])
        # Update discriminator for B -> [real/false]
        dB_loss1 = d_model_B.train_on_batch(X_realB, y_realB)
        dB_loss2 = d_model_B.train_on_batch(X_fakeB, y_fakeB)

        # Summarize performance
        print('Iteration>%d, dA[%.3f, %.3f] dB[%.3f, %.3f] g[%.3f, %.3f]' %
              (i+1, dA_loss1, dA_loss2, dB_loss1, dB_loss2, g_loss1, g_loss2))

        # Evaluate the model performance periodically
        # If batch size (total images) = 100, performance will be summarized after every 75th iteration.
        if (i+1) % (bat_per_epo * 1) == 0:
            # Plot A -> B translation
            try :
               summarize_performance(i, g_model_AtoB, trainA, 'AtoB')
            except :
                print("Error in AtoB")
            # Plot B -> A translation
            try :
                summarize_performance(i, g_model_BtoA, trainB, 'BtoA')
            except :
                print("Error in BtoA")
        if (i+1) % (bat_per_epo * 5) == 0:
            # Save the model
            # If batch size (total image) = 100, model will be saved after every 75th iteration x 5 = 375 iterations.
            try :
                save_models(i, g_model_AtoB, g_model_BtoA)
            except :
                print("Error in save_models")