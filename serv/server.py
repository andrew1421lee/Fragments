#!/usr/bin/env python
from flask import Flask, jsonify, request
from math import sin, cos, sqrt, atan2, radians
import sqlite3
import sys
import signal

app = Flask(__name__)

conn = sqlite3.connect('data.db')
cursor = conn.cursor()

# Handle control-c so database doesnt get corrupted
def handler(signum, frame):
    print "Here you go"
    conn.commit()
    conn.close()
    sys.exit()

@app.route("/")
def hello():
    return "Hello!!!"

@app.route("/init")
def init():
    cursor.execute("CREATE TABLE points (date date, lat decimal(3,6), long decimal(3,6), author text, message text)")
    return "Database initialized"

@app.route("/create_test")
def make_test():
    cursor.execute("INSERT INTO points VALUES ('2016-07-11', 42.094372, -75.959462, 'Anchu Lee', 'The bathroom here is really nice')")
    cursor.execute("INSERT INTO points VALUES ('2018-02-10', 42.094044, -75.959191, 'Anchu Lee', 'Do not order the turkey sandwitch')") 

    return "Test made" 

@app.route("/get_points/<string:bounds>", methods=['GET'])
def get_points(bounds):
    points = bounds.split('=')
    toplat = float(points[0].split(':')[0])
    toplong = float(points[0].split(':')[1])

    botlat = float(points[1].split(':')[0])
    botlong = float(points[1].split(':')[1])
    
    q = (botlat, toplat, botlong, toplong)
    cursor.execute("SELECT * FROM points")
    print cursor.fetchone()

    cursor.execute("SELECT lat, long FROM points WHERE lat BETWEEN ? AND ? AND long BETWEEN ? AND ?", q)

    print "tlat = " + str(toplat) + ", tlong = " + str(toplong) + ", blat = " + str(botlat) + ", blong = " + str(botlong)
    json = jsonify(cursor.fetchall())
    #print json
    return json

@app.route("/get_marker/<string:coord>", methods=['GET'])
def get_marker(coord):
    points = coord.split('=')
    clat = abs(radians(float(points[0].split(':')[0])))
    clong = abs(radians(float(points[0].split(':')[1])))

    mlt = float(points[1].split(':')[0])
    mln = float(points[1].split(':')[1])

    mlat = abs(radians(mlt))
    mlong = abs(radians(mln))

    # print clat

    R = 6373

    dlat = abs(clat - mlat)
    dlong = abs(clong - mlong)

    # print dlat
    a = sin(dlat / 2)**2 + cos(clat) * cos(mlat) * sin(dlong / 2)**2

    c = 2 * atan2(sqrt(a), sqrt(1 - a))

    distance = R * c * 1000
    print "Distance(m) is", distance

    if distance < 100:
        q = (mlt - 0.00005, mlt + 0.00005, mln - 0.00005, mlt + 0.00005)
        print q
        cursor.execute("SELECT date, author, message FROM points WHERE lat BETWEEN ? AND ? AND long BETWEEN ? AND ?", q)
        string = cursor.fetchone()
        print string
        json = jsonify(string)
        print str(json)
        return json

    return "Fail"


@app.route("/new_point", methods=['POST'])
def new_point():
    text = request.data
    elements = text.split("|||")
    if len(elements) != 5:
        return "Falure";
    
    lat = float(elements[0])
    lng = float(elements[1])
    date = elements[2]
    name = elements[3]
    msg = elements[4]
    
    q = (date, lat, lng, name, msg)
    cursor.execute("INSERT INTO points VALUES (?, ?, ?, ?, ?)", q)

    print text
    return "Data received"


signal.signal(signal.SIGINT, handler) 
