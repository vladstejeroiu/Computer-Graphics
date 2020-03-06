# Computer-Graphics

This project was part of my university degree. The **aims** of this coursework were:
* Understand how an image is stored internally, and how to manipulate the image.
* Translate useful graphics algorithms into working code.
* Understand that graphics can be useful to users (in this case within the medical context).
* Work with a three-dimensional data set.
* Combine interaction with visual feedback.

# Exercise
Implement the following:
* Display a slider to allow the user to move through slices arbitrarily.
* Display front and side views in addition to the top view (with independent sliders for each view).
* Perform maximum intensity projection. Implement **MIP** for all three views: front view, side view and top view (it does not matter which direction you choose for the side view).
* Display thumbnail images for all slices of the data set (here I used the **Nearest Neighbour** algorithm).
*  Allow the image to be resized(using **Bilinear Interpolation**).
* Perform histogram equalization on the data set (not on the images) to scale from the signed short int range to the unsigned byte range. 
