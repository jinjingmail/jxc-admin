'use strict'
const path = require('path')
const settings = require('./src/config')
const CompressionWebpackPlugin = require('compression-webpack-plugin')
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin
const ThemeColorReplacer = require('webpack-theme-color-replacer')
const forElementUI = require('webpack-theme-color-replacer/forElementUI')

const EMPTY_PLUGIN = {apply: () => ({})}

function resolve(dir) {
    return path.join(__dirname, dir)
}

// All configuration item explanations can be find in https://cli.vuejs.org/config/
module.exports = {
    publicPath: settings.contextPath,
    outputDir: 'dist',
    assetsDir: 'static',
    runtimeCompiler: true,
    lintOnSave: false,
    productionSourceMap: settings.isDev,
    parallel: true,
    devServer: {
        port: process.env.port || 8079,
        contentBasePublicPath: settings.contextPath,
        open: true,
        overlay: {
            warnings: true,
            errors: true
        },
        proxy:
            settings.useMock
                ? null
                : {
                    [settings.apiPrefix]: {
                        target: 'http://localhost:8081',  //后台接口域名
                        ws: true,                         //如果要代理 websockets，配置这个参数
                        secure: false,                    //如果是https接口，需要配置这个参数
                        changeOrigin: true,               //是否跨域
                        pathRewrite: {
                            [`^${settings.apiPrefix}`]: ''
                        }
                    }
                },
        before(app) {
            require('./mock')(app)
        }
    },
    configureWebpack: {
        name: settings.title,
        resolve: {
            alias: {
                '@': resolve('src'),
                '@ele': resolve('element-ui-personal')
            }
        },
        plugins: [
            new CompressionWebpackPlugin({
                filename: '[path].gz[query]',
                algorithm: 'gzip',
                test: new RegExp('\\.(js|css)$'),//匹配文件名
                threshold: 10240,//对10K以上的数据进行压缩
                minRatio: 0.8,
                deleteOriginalAssets: false,//是否删除源文件
            }),
            settings.isDev
                ? new ThemeColorReplacer({
                    fileName: 'css/theme-colors.[contenthash:8].css',
                    matchColors: forElementUI.getElementUISeries('#1890ff'),
                    changeSelector: forElementUI.changeSelector
                })
                : EMPTY_PLUGIN,
            settings.isDev ? EMPTY_PLUGIN : new BundleAnalyzerPlugin()
        ]
    },
    chainWebpack(config) {
        config.plugins.delete('preload')
        config.plugins.delete('prefetch')

        // set svg-sprite-loader
        config.module
            .rule('svg')
            .exclude.add(resolve('src/assets/icons'))
            .end()
        config.module
            .rule('icons')
            .test(/\.svg$/)
            .include.add(resolve('src/assets/icons'))
            .end()
            .use('svg-sprite-loader')
            .loader('svg-sprite-loader')
            .options({symbolId: 'icon-[name]'})
            .end()
    }
}
