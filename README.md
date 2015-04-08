# accelerama
Stream data gathering compression library and showcase

## Integrating accelerama into your app
In your <code>onSensorChanged</code> method, when capturing data coming in fast like light or acceleration data,
simply store your points in an:

ArrayList of Floats for 1-dimensional data

AccelerationCollection for 3-dimensional data

Next, choose your <code>AccelerationCompressor</code> or <code>LightCompressor</code> and use
the read method to compress your data to a smaller format.

When reading later, simply use the same <code>Compressor</code>'s read method.
