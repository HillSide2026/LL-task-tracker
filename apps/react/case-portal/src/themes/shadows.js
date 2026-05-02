// material-ui
import { alpha } from '@mui/material/styles'

// ==============================|| DEFAULT THEME - CUSTOM SHADOWS  ||============================== //

const CustomShadows = (theme) => ({
  button: 'none',
  text: 'none',
  z1: `0px 1px 3px ${alpha(theme.palette.grey[900], 0.1)}`,
  // only available in paid version
})

export default CustomShadows
