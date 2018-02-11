#!/usr/bin/env python
from flask import Flask, jsonify
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
    cursor.execute("INSERT INTO points VALUES ('2018-02-11', 42.094372, -75.959462, 'Anchu Lee', 'Where is the bathroom?')")
    cursor.execute("INSERT INTO points VALUES ('2018-02-10', 42.094044, -75.959191, 'Anchu Lee', 'One time it took almost 40 minutes to get my food')") 

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

    cursor.execute("SELECT * FROM points WHERE lat BETWEEN ? AND ? AND long BETWEEN ? AND ?", q)

    print "tlat = " + str(toplat) + ", tlong = " + str(toplong) + ", blat = " + str(botlat) + ", blong = " + str(botlong)
    json = jsonify(cursor.fetchall())
    print json
    return json

signal.signal(signal.SIGINT, handler) 
