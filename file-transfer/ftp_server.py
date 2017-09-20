#!/usr/bin/python

'''
A File Transfer Protocol (FTP) server that allows clients to upload and download files

Usage: python ftp_server.py <port>
'''

import os
import socket
import argparse

class FTP_Server(object):

	def __init__(self, port = None):
		print('In constructor: {}'.format(port))
		# Initialize a streaming socket
		self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		if port is None:
			port = 12345
		
		self.server_socket.bind((socket.gethostname(), port))
		self.server_socket.listen(5)

		print('Start server with host {} and port number {}'.format(socket.gethostname(), port))

		self.run_tasks()

	def run_tasks(self):
		"""
		Run tasks on server side
		"""
		while True:
			# Accept connection from client
			(client_socket, address) = self.server_socket.accept()
			print('Connected to client at {}'.format(address))
			
			requested_file = 'output.txt'

			command = client_socket.recv(1024).decode()
			print('Got command from client: {}'.format(command))

			if command == 'send':
				self.receive_text_file(client_socket, requested_file)
			elif command == 'download':
				self.send_text_file(client_socket, requested_file)
			else:
				print('Invalid command! Close connection.')
				client_socket.close()
			print('Server shutting down...')
			return

	def receive_text_file(self, socket, file, chunk_size=4098):
		'''
		Receive text file sent from client
		'''
		with open(file, 'w') as f:
			data = socket.recv(chunk_size)
			while data:
				f.write(data.decode())
				data = socket.recv(chunk_size)

		socket.close()

	def send_text_file(self, socket, file):
		"""
		Send requested text file to client
		"""
		if not os.path.isfile(file):
			print('File does not exist on server\' file system. Shutdown server...')
			socket.close()
			return

		with open(file, 'r') as f:
			for line in f:
				socket.send(line.encode())

		# Finish sending file to client, close socket
		socket.close()

def main():
	parser = argparse.ArgumentParser('python ftp_server.py <port-number>')
	parser.add_argument('port', type=int, help='Server\'s port number')
	args = vars(parser.parse_args())

	server = FTP_Server(args['port'])

main()		