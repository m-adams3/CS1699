from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

db = SQLAlchemy()

class Pitt(db.Model):
	pitt_id = db.Column(db.Integer, primary_key=True)
	name = db.Column(db.String(24), unique=True, nullable=False)
	role = db.Column(db.String(24), nullable=False)
	permission = db.Column(db.String(24), nullable=False)
	department = db.Column(db.String(24), nullable=False)

	def __init__(self, name, role, permission, department):
		self.name = name
		self.role = role
		self.permission = permission
		self.department = department

	# tell Python how to print
	def __repr__(self):
		return '<Pitt {}>'.format(self.name)

class Graduate(db.Model):
	grad_id = db.Column(db.Integer, primary_key=True)
	pitt_id = pitt_id = db.Column(db.Integer, nullable=False)

	def __init__(self, pitt_id):
		self.pitt_id = pitt_id

	def __repr__(self):
		return '<Graduate {}>'.format(self.pitt_id)

class CS(db.Model):
	cs_id = db.Column(db.Integer, primary_key=True)
	pitt_id = db.Column(db.Integer, nullable=False)

	def __init__(self, pitt_id):
		self.pitt_id = pitt_id

	# tell Python how to print
	def __repr__(self):
		return '<CS {}>'.format(self.pitt_id)