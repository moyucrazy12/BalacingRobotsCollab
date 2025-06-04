from scipy.spatial import distance as dist
from imutils import perspective
from imutils import contours
from client import Client
import numpy as np
import argparse
import imutils
import cv2

from socket import socket
from socket import AF_INET
from socket import SOCK_STREAM
from socket import SHUT_RDWR

def midpoint(ptA, ptB):
	return ((ptA[0] + ptB[0]) * 0.5, (ptA[1] + ptB[1]) * 0.5)

#Server
conn = Client()
conn.connect(4444) # IP-address implicit
print('Connection to host established')

# Capturing video through webcam
webcam = cv2.VideoCapture(0)
color = (0, 0, 255)
refObj = None
# Start a while loop
while(1):
	
	# Reading the video from the
	# webcam in image frames
	_, imageFrame = webcam.read()

	# Convert the imageFrame in
	# BGR(RGB color space) to
	# HSV(hue-saturation-value)
	# color space
	hsvFrame = cv2.cvtColor(imageFrame, cv2.COLOR_BGR2HSV)

	# Set range for blue color and
	# define mask
	#blue_lower = np.array([100, 100, 100], np.uint8)
	#blue_upper = np.array([110, 255, 255], np.uint8)
	#blue_mask = cv2.inRange(hsvFrame, blue_lower, blue_upper)
	
	red_lower = np.array([0, 50, 50], np.uint8)
	red_upper = np.array([10, 255, 255], np.uint8)
	red_mask = cv2.inRange(hsvFrame, red_lower, red_upper)
	
	kernel = np.ones((5, 5), "uint8")
	
	# # For blue color
	# blue_mask = cv2.dilate(blue_mask, kernel)
	# res_blue = cv2.bitwise_and(imageFrame, imageFrame,
	#						mask = blue_mask)
	
	# # For blue color
	red_mask = cv2.dilate(red_mask, kernel)
	res_red = cv2.bitwise_and(imageFrame, imageFrame,
							mask = red_mask)

	# # Creating contour to track red color
	contours, hierarchy = cv2.findContours(red_mask,
										cv2.RETR_TREE,
										cv2.CHAIN_APPROX_SIMPLE)
	for pic, contour in enumerate(contours):
		area = cv2.contourArea(contour)
		if(area > 300):
			box = cv2.minAreaRect(contour)
			box = cv2.cv.BoxPoints(box) if imutils.is_cv2() else cv2.boxPoints(box)
			box = np.array(box, dtype="int")
			box = perspective.order_points(box)
			
			x, y, w, h = cv2.boundingRect(contour)
			cX = x+w/2
			cY = y+h/2
			imageFrame = cv2.rectangle(imageFrame, (x, y),
									(x + w, y + h),
									(255, 0, 0), 2)
			
			cv2.putText(imageFrame, "Red Colour", (x, y),cv2.FONT_HERSHEY_SIMPLEX,	1.0, (255, 0, 0))
			cv2.circle(imageFrame, (int(cX), int(cY)), 5, (0, 0, 255), -1)
			cv2.putText(imageFrame, "{:.1f}".format(cX), (int(cX-60), int(cY+15)), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
			cv2.putText(imageFrame, "{:.1f}".format(cY), (int(cX), int(cY+15)), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)


			if refObj is None:
				(tl, tr, br, bl) = box
				(tlblX, tlblY) = midpoint(tl, bl)
				(trbrX, trbrY) = midpoint(tr, br)
				Dc = dist.euclidean((tlblX, tlblY), (trbrX, trbrY))
				# refObj = (box, (cX, cY), D / 1)     #width in cm 
				refCx = cX
				refCy = cY
				refObj = (cX, cY)
				print(refObj)
				continue
			# refCoords = np.vstack([refObj[0], refObj[1]])
			# objCoords = np.vstack([box, (cX, cY)])
			# for ((xA, yA), (xB, yB), color) in zip(refCoords, objCoords, color):
			# 	cv2.circle(imageFrame, (int(xA), int(yA)), 5, color, -1)
			# 	cv2.putText(imageFrame, "{:.1f}".format(yA), (int(xA), int(yA+15)),
		    #         cv2.FONT_HERSHEY_SIMPLEX, 0.55, color, 2)
			# 	cv2.circle(imageFrame, (int(xB), int(yB)), 5, color, -1)
			# 	cv2.line(imageFrame, (int(xA), int(yA)), (int(xB), int(yB)),
            #         color, 2)
			# 	D = dist.euclidean((xA, yA), (xB, yB)) / refObj[2]
			# 	(mX, mY) = midpoint((xA, yA), (xB, yB))
			# 	cv2.putText(imageFrame, "{:.1f}in".format(D), (int(mX), int(mY - 10)),
		    #         cv2.FONT_HERSHEY_SIMPLEX, 0.55, color, 2)
			# cv2.circle(imageFrame, (int(xA), int(yA)), 5, color, -1)
			# cv2.putText(imageFrame, "{:.1f}".format(yA), (int(xA), int(yA+15)),	cv2.FONT_HERSHEY_SIMPLEX, 0.55, color, 2)
			# cv2.circle(imageFrame, (int(xB), int(yB)), 5, color, -1)
			
			cv2.line(imageFrame, (int(cX), int(cY)), (int(refCx),int(refCy)),color, 2)
			D = dist.euclidean((cX, cY), ((refCx),(refCy))) / (Dc/2.5) #distance in mm
			conn.send(str(D))
			echo = conn.receive()
			if echo != 'Ok':
				print("Something went wrong in receiving")
			#time.sleep(0.1)
			(mX, mY) = midpoint((cX, cY), ((refCx),(refCy)))
			cv2.putText(imageFrame, "{:.1f}cm".format(D), (int(mX), int(mY - 10)), cv2.FONT_HERSHEY_SIMPLEX, 0.55, color, 2)



	refObj = None
	# Program Termination
	cv2.imshow("Multiple Color Detection in Real-TIme", imageFrame)
	# cv2.imshow("RED", res_red)
	cv2.imshow("RED", res_red)
	# cv2.imshow("GREEN", res_green)
	if cv2.waitKey(10) & 0xFF == ord('q'):
		cap.release()
		cv2.destroyAllWindows()
		break