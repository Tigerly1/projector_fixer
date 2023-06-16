from flask import Blueprint, render_template, request, flash, jsonify, url_for, app
import json
import pyautogui
import numpy as np
import cv2
import time
import glob
import shutil
import os

views = Blueprint('views', __name__)


@views.route('/', methods=['GET', 'POST'])
def home():
    imag1 = url_for('static', filename='image1.jpg')
    imag2 = url_for('static', filename='image2.jpg')
    return render_template("home.html", image1=imag1, image2=imag2)


# function to show test green image in seperate window
@views.route('/display_test_image')
def display_test_image():
    print("Displaying test image")
    cv2.namedWindow("Live", cv2.WINDOW_NORMAL)
    frame = np.zeros((2160, 3840, 3))
    frame[:, :, 1] = np.ones((2160, 3840)) * 255
    cv2.imshow('Live', frame)
    while True:
        if cv2.waitKey(1) == ord('q'):
            break
    cv2.destroyAllWindows()
    return '', 204


# function to copy recent photos from /images to /static and refresh home
@views.route('/refresh_photos')
def refresh_photos():
    print("Refreshing photos")
    src_dir = "C:/Users/Marcin/Desktop/Flask-Web-App-Tutorial-main/Flask-Web-App-Tutorial-main/website/images/"
    dst_dir = "C:/Users/Marcin/Desktop/Flask-Web-App-Tutorial-main/Flask-Web-App-Tutorial-main/website/static/"
    os.remove(dst_dir + 'image1.jpg')
    os.remove(dst_dir + 'image2.jpg')
    for jpgfile in glob.iglob(os.path.join(src_dir, "*.jpg")):
        shutil.copy(jpgfile, dst_dir)
    imag1 = url_for('static', filename='image1.jpg')
    imag2 = url_for('static', filename='image2.jpg')
    return render_template("home.html", image1=imag1, image2=imag2)


# function to calculate parameters for perspective correction
@views.route('/calculate_parameters')
def calculate_parameters():
    print("Calculating model parameters")
    return '', 204


# function to share screen in another window
@views.route('/share_screen')
def share_screen():
    print("Sharing screen")
    screen_width, screen_height = pyautogui.size()
    resolution = (screen_width, screen_height)
    cv2.namedWindow("Live", cv2.WINDOW_NORMAL)
    cv2.resizeWindow("Live", 480, 270)
    while True:
        img = pyautogui.screenshot()
        frame = np.array(img)
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        cv2.imshow('Live', frame)
        if cv2.waitKey(1) == ord('q'):
            break
    cv2.destroyAllWindows()
    return '', 204
