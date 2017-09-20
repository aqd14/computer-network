#!/usr/bin/python           # This is ftp_client.py file

'''
A File Transfer Protocol (FTP) client used to send or receive files from server

Usage: python ftp_client.py <command> <filepath> ... <filepath> <hostname> <port-number>
'''

import os
import socket
import argparse

class FTP_Client(object):

	def __init__(self, host, port, command, filepath):
		'''
		Constructor

		+ host: server host name
		+ port: port number
		+ command: either 'send' or 'download' file from server
		+ filepath: file path from client side, if command is 'send' or file name if user is downloading file from server
		'''

		# Create an INET, Streaming socket
		self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		# Bind socket to server's host and port number
		self.connect(host, port)

		self.client_socket.send(command.encode()) # Send request to server
		
		if command == 'send':
			self.send_text_file(filepath)
		elif command == 'download':
			self.download_text_file(filepath)
		else:
			raise AttributeError('Invalid command! Should be either \'send\' or \'download\'')

		# Close socket after finish request
		self.client_socket.close()


	def connect(self, host, port):
		# Default port number
		if port is None:
			port = 12345

		# Bind socket to given host and port number
		print('Connect to server at host: {} and port: {}'.format(host, port))
		# self.client_socket.connect((host, port))
		self.client_socket.connect((socket.gethostname(), port))


	def send_text_file(self, filepath):
		"""
		Send file to server
		"""

		if not os.path.isfile(filepath):
			raise RuntimeError('File is not exist!')
		
		# Read content of file on client side to send to server
		with open(filepath, 'r') as f:
			# chunk = f.read(chunk_size)
			# while chunk:
			# 	sent_success = self.client_socket.send(chunk) # Number of bytes sent successfully
			# 	print('Bytes sent: ', sent_success)

			for line in f:
				sent_success = self.client_socket.send(line.encode()) # Number of bytes sent successfully
				print('Bytes sent: ', sent_success)


	def download_text_file(self, file, chunk_size=4096):
		"""
		Request to download file from server
		"""

		with open(file, 'w') as f:
			data = self.client_socket.recv(chunk_size)
			while data:
				f.write(data.decode())
				data = self.client_socket.recv(chunk_size)


def main():
	
	parser = argparse.ArgumentParser('python ftp_client.py <hostname> <port-number> <command> <filepath>')
	# positional arguments
	parser.add_argument('hostname', type=str, help='A server\'s host name')
	parser.add_argument('port', type=int, help='Server\'s port number')
	parser.add_argument('filepath', type=str, help='A file to send or download')
	# optional arguments
	parser.add_argument('--command', type=str, default='send', help='The service request to server. Can be either send file or download file (default: send)')

	args = vars(parser.parse_args())

	# print()
	# if len(args) == 4
	client = FTP_Client(args['hostname'], args['port'], args['command'], args['filepath'])

main()