// material-ui
import { createTheme } from '@mui/material/styles'

// third-party
import { presetPalettes } from '@ant-design/colors'

// project import
import ThemeOption from './theme'

// ==============================|| DEFAULT THEME - PALETTE  ||============================== //

const Palette = (mode) => {
  const colors = presetPalettes

  const greyPrimary = [
    '#ffffff',
    '#fbfaf6',
    '#f5f2ea',
    '#ebe5da',
    '#d6cec1',
    '#b4aa9d',
    '#81776d',
    '#5e5750',
    '#2a2927',
    '#1f2328',
    '#121417',
  ]
  const greyAscent = ['#fbfaf6', '#b4aa9d', '#3f444a', '#20242a']
  const greyConstant = ['#f8f5ee', '#e7dfd1']

  colors.grey = [...greyPrimary, ...greyAscent, ...greyConstant]

  const paletteColor = ThemeOption(colors)

  return createTheme({
    palette: {
      mode,
      common: {
        black: '#121417',
        white: '#fff',
      },
      ...paletteColor,
      text: {
        primary: paletteColor.grey[700],
        secondary: paletteColor.grey[500],
        disabled: paletteColor.grey[400],
      },
      action: {
        disabled: paletteColor.grey[300],
      },
      divider: paletteColor.grey[200],
      background: {
        paper: '#fffdf8',
        default: '#f8f5ee',
      },
    },
  })
}

export default Palette
