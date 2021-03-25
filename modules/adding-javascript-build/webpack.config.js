const path = require('path');

const PUBLIC_PATH = '/o/adding-javascript-build/';

module.exports = {
	mode: 'production',
	context: path.resolve(__dirname),
	entry: './src/main/resources/META-INF/resources/js',
	module: {
		rules: [
			{
				test: /\.tsx?$/,
				use: 'ts-loader',
				exclude: /node_modules/,
			},
		],
	},
	resolve: {
		extensions: ['.tsx', '.ts', '.js'],
	},
	output: {
		filename: 'js/bundle.js',
		libraryTarget: 'window',
		path: path.resolve('./build/resources/main/META-INF/resources/'),
		publicPath: PUBLIC_PATH,
	},
};
