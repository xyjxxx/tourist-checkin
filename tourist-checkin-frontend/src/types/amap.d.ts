declare module '@amap/amap-jsapi-loader' {
  interface AMapLoaderOptions {
    key: string
    version: string
    plugins?: string[]
    securityConfig?: {
      securityJsCode?: string
    }
  }
  const loader: {
    load: (options: AMapLoaderOptions) => Promise<any>
  }
  export default loader
}

declare global {
  interface Window {
    AMap: any
    _AMapSecurityConfig?: {
      securityJsCode?: string
    }
  }
}

export {}
