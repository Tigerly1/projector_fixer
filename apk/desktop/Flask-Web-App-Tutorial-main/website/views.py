from flask import Blueprint, render_template, request, flash, jsonify, url_for, app
import json
import pyautogui
import numpy as np
import cv2
import time
import glob
import shutil
import os

from website.params import ParamsSaver

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
    screen_width, screen_height = pyautogui.size()
    cv2.namedWindow("Live", cv2.WND_PROP_FULLSCREEN)
    cv2.setWindowProperty("Live", cv2.WND_PROP_FULLSCREEN, cv2.WINDOW_FULLSCREEN)
    # cv2.resizeWindow("Live", 2160, 3840)
    cv2.moveWindow("Live", screen_width, 0)
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
# function to share screen in another window
@views.route('/share_screen')
def share_screen():
    print("Sharing screen")
    # M = np.array([[4.6656708905268225, 0.962262589782765, -1416.3209959113651], [-1.2595302756020346, -2.96466686073545, 2505.0071003838325], [0.0012798618451005134, 0.002215097207902191, 1.0]])
    M = np.array([[0.5, 0.0, 0.0],
                       [0.0, 0.3, 0.0],
                       [0.0, 0.0, 0.7]])
    
    params_saver = ParamsSaver()
    params = params_saver.get_value()
    if params is not None:
        M = np.array(params)
    screen_width, screen_height = pyautogui.size()
    resolution = (screen_width, screen_height)
    cv2.namedWindow("Live", cv2.WND_PROP_FULLSCREEN)
    cv2.setWindowProperty("Live", cv2.WND_PROP_FULLSCREEN, cv2.WINDOW_FULLSCREEN)
    # cv2.resizeWindow("Live", 2160, 3840)
    cv2.moveWindow("Live", screen_width, 0)
    x=0
    while True:
        img = pyautogui.screenshot()
        frame = np.array(img)
        # frame = cv2.resize(frame, (3840, 2160))
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        frame = cv2.warpPerspective(frame, M, (frame.shape[1], frame.shape[0]))
        if x==0:
            cv2.imwrite('website/static/ex.jpg', frame)
            x=1
        cv2.imshow('Live', frame)
        if cv2.waitKey(1) == ord('q'):
            break
    cv2.destroyAllWindows()
    return '', 204
