# import required modules
import time
import os
import json
from pprint import pprint
from flask import Flask, request, session, url_for, redirect, render_template, abort, g, flash, _app_ctx_stack, jsonify
from sqlalchemy import or_

# import db stuff
from models import db, Pitt, Graduate, CS

# create app as instance of flask class
app = Flask(__name__)
app.config['TEMPLATES_AUTO_RELOAD'] = True

# configuration
SQLALCHEMY_DATABASE_URI = 'sqlite:///' + os.path.join(app.root_path, 'access.db')
SQLALCHEMY_TRACK_MODIFICATIONS = True
SECRET_KEY = 'development key'
app.config.from_object(__name__)
db.init_app(app)

# create access policy by parsing JSON file and creating db entries
def create_access_policy():
	with open('access_policy2.json') as json_data:
		data = json.load(json_data)
		#print(data["Pitt"][0]["name"])

		# create Pitt database
		for person in data["Pitt"]:
			# print(person["name"])
			db.session.add(Pitt(person["name"], person["role"], person["permission"], person["department"]))
			db.session.commit()

		# create Graduate database
		graduates = Pitt.query.filter_by(role="graduate").all()
		
		for grad in graduates:
			test = grad.pitt_id
			db.session.add(Graduate(test))
			db.session.commit()

		# print(Graduate.query.all())

		# create CS database
		cs = Pitt.query.filter_by(department="CS").all()
		for c in cs:
			test = c.pitt_id
			db.session.add(CS(test))
			db.session.commit()

		# print(CS.query.all())

# get pitt_id from Pitt.name
def get_id_from_name(name):
	rv = Pitt.query.filter_by(name=name).first()
	return rv.pitt_id if rv else None

@app.cli.command('initdb')
def initdb_command():
	db.drop_all()
	db.create_all()
	before = int(time.time())
	print("{}{}".format("Before: ", before))
	create_access_policy()
	after = int(time.time())
	print("{}{}".format("After: ", after))
	delta = after - before
	print("{}{}".format("Delta: ", delta))
	print('Initialized the database.')

@app.route('/')
@app.route('/index')
def index():
	people = Pitt.query.all()
	return render_template('index.html', people=people)

@app.route('/indirection', methods=['POST'])
def indirection():
	# do query, flash appropriate message
	if Graduate.query.filter_by(pitt_id=get_id_from_name(request.form['name'])).first():
		flash(request.form['name'] + " is a Graduate student: Access Granted")
	else:
		flash(request.form['name'] + " is not a Graduate student: Access Denied")
	# return redirect to index
	return redirect(url_for('index'))

@app.route('/delegation', methods=['POST'])
def delegation():
	# do two part query, flash appropriate message
	if CS.query.filter_by(pitt_id=get_id_from_name(request.form['name'])).first():
		person = Pitt.query.filter_by(name=request.form['name']).first()
		if person.role == request.form['role']:
			flash("CS department confirms that " + request.form['name'] + " is a " +  request.form['role'] + ": Access Granted")
	else:
		flash("CS department does not confirm that " + request.form['name'] + " is a " +  request.form['role'] + ": Access Denied")
	# return redirect to index
	return redirect(url_for('index'))

@app.route('/intersection', methods=['POST'])
def intersection():
	# do query
	if CS.query.filter_by(pitt_id=get_id_from_name(request.form['name'])).first():
		flash(request.form['name'] + " is a member of the CS department: Access Granted")
	elif Graduate.query.filter_by(pitt_id=get_id_from_name(request.form['name'])).first():
		flash(request.form['name'] + " is a graduate student: Access Granted")
	else:
		flash(request.form['name'] + " is not in CS nor is a graduate student: Access Denied")
	# get result
	# return redirect to index
	return redirect(url_for('index'))

@app.route('/inference', methods=['POST'])
def inference():
	# do query
	person = Pitt.query.filter_by(name=request.form['name']).first()
	if person.permission == "read":
		flash(request.form['name'] + " has read only permission and is therefore an undergrad: Access Granted")
	else:
		flash("Unable to infer role based on permission status: Access Denied")
	# return redirect to index
	return redirect(url_for('index'))